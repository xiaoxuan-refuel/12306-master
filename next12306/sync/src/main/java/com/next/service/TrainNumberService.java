package com.next.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.next.common.TrainEsConstant;
import com.next.dao.TrainNumberDetailMapper;
import com.next.dao.TrainNumberMapper;
import com.next.model.TrainNumber;
import com.next.model.TrainNumberDetail;
import com.next.model.TrainSeat;
import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Title: TrainNumberService
 * @Description:
 * @author: tjx
 * @date :2022/9/28 18:02
 */
@Service
@Slf4j
public class TrainNumberService {

    @Autowired
    private TrainNumberMapper trainNumberMapper;

    @Autowired
    private TrainNumberDetailMapper trainNumberDetailMapper;

    @Autowired
    private TrainCacheService trainCacheService;

    @Autowired
    private EsClient esClient;


    public void handle(List<CanalEntry.Column> columns, CanalEntry.EventType eventType) throws Exception {
        if (eventType != CanalEntry.EventType.UPDATE) {
            log.info("not update, no need care ");
            return;
        }
        int trainNumberId = 0;
        for (CanalEntry.Column column : columns) {
            if (column.getName().equals("id")) {
                trainNumberId = Integer.valueOf(column.getValue());
                break;
            }

        }
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(trainNumberId);
        if (trainNumber == null) {
            log.error("not found trainNumber, trainNumberId: {}",trainNumberId);
            return;
        }
        List<TrainNumberDetail> trainNumberDetailList = trainNumberDetailMapper.getByTrainNumberId(trainNumberId);
        if (CollectionUtils.isEmpty(trainNumberDetailList)) {
            log.warn("no detail, no need care trainNumber:{}",trainNumber.getName());
            return;
        }

        trainCacheService.set("TN_"+trainNumber.getName(), JsonMapper.obj2String(trainNumberDetailList));
        log.info("trainNumber: {} detailList update redis success",trainNumber.getName());

        //将车次详情信息的站到站的信息保存到Es中
        saveEs(trainNumberDetailList,trainNumber);
        log.info("trainNumber: {} detailList update ES success",trainNumber.getName());
    }

    /** 该方法保存的数据是为了满足前台用户 根据车站与车站之间的查询
     *  fromStationId -> toStationId 例如：从北京到大连的车次 可能有G123，G456等多个车次信息
     *  trainNumber G123: 北京->唐山->大连 G456
     *  其中G123车次详情可以分为： 北京->锦州 锦州->大连 北京->大连
     *  其中G456车次详情可以分为： 北京->鞍山 鞍山->大连 北京->大连
     *  需要保存到ES中的数据为：(北京->大连)fromStationId_toStationId : G123,G456....等
     * @param detailList
     * @param trainNumber
     */
    public void saveEs(List<TrainNumberDetail> detailList,TrainNumber trainNumber) throws Exception{
        //这里将List数组指定了长度，如果不指定长度避免车次信息过多的话导致List频繁触发扩容机制
        List<String> stationList = Lists.newArrayListWithCapacity(detailList.size() << 2);
        if(detailList.size() == 1){
            Integer fromStationId = trainNumber.getFromStationId();
            Integer toStationId = trainNumber.getToStationId();
            stationList.add(fromStationId +"_"+toStationId);
        }else{//如果是多端路程，则进行遍历 前提必须保证每个数据中的station_index有序
            for (int i = 0; i < detailList.size(); i++) {
                Integer fromStationId = detailList.get(i).getFromStationId();
                for (int j = i; j < detailList.size(); j++) {
                    Integer toStationId = detailList.get(j).getToStationId();
                    stationList.add(fromStationId +"_"+toStationId);
                }
            }
        }
        //组装批量请求，获取es已存储的数据
        MultiGetRequest getRequest = new MultiGetRequest();
        for (String item : stationList) {
            getRequest.add(new MultiGetRequest.Item(TrainEsConstant.INDEX, TrainEsConstant.TYPE,item));
        }
        MultiGetResponse multiGetItemResponses = esClient.multiGet(getRequest);
        //批量新增/更新数据操作
        BulkRequest bulkRequest = new BulkRequest();
        //遍历ES处理的每一项
        for (MultiGetItemResponse itemResponse : multiGetItemResponses.getResponses()) {
            if (itemResponse.isFailed()) {
                log.error("multiGet item failed , itemResponse:{}",itemResponse);
                continue;
            }
            GetResponse getResponse = itemResponse.getResponse();
            if (getResponse == null) {
                log.error("multiGet item get response is null, itemResponse:{}",itemResponse);
                continue;
            }
            //保存es数据的Map
            Map<String,Object> data = Maps.newHashMap();
            //获取结果
            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
            //判断如果没有根据索引名称查询到对应的item数据或者getSourceAsMap获取到的数据为空
            if (!getResponse.isExists() || sourceAsMap.isEmpty()) {
                //添加数据到索引库中
                data.put(TrainEsConstant.COLUMN_TRAIN_NUMBER,trainNumber.getName());
                IndexRequest indexRequest = new IndexRequest(TrainEsConstant.INDEX, TrainEsConstant.TYPE, getResponse.getId()).source(data);
                bulkRequest.add(indexRequest);
                continue;
            }
            String origin = (String) sourceAsMap.get(TrainEsConstant.COLUMN_TRAIN_NUMBER);
            Set<String> trainNumberSets = Sets.newHashSet(Splitter.on(",").omitEmptyStrings().trimResults().split(origin));
            if (!trainNumberSets.contains(trainNumber.getName())) {
                data.put(TrainEsConstant.COLUMN_TRAIN_NUMBER,origin +","+trainNumber.getName());
                UpdateRequest updateRequest = new UpdateRequest(TrainEsConstant.INDEX, TrainEsConstant.TYPE, getResponse.getId()).doc(data);
                bulkRequest.add(updateRequest);
            }
        }

        BulkResponse bulk = esClient.bulk(bulkRequest);
        log.info("es bulk, response:{}", JsonMapper.obj2String(bulk));
        //获取批量更新结果
        if(bulk.hasFailures()){
            throw new RuntimeException("es bulk exception");
        }
    }
}
