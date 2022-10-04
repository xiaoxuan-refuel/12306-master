package com.next.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.next.common.TrainEsConstant;
import com.next.dto.TrainNumberLeftDto;
import com.next.model.TrainNumber;
import com.next.model.TrainNumberDetail;
import com.next.param.SearchLeftCountParam;
import com.next.util.BeanValidator;
import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.type.TypeReference;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @Title: TrainSeatService
 * @Description:
 * @author: tjx
 * @date :2022/10/3 16:25
 */
@Service
@Slf4j
public class TrainSeatService {

    @Autowired
    private EsClient esClient;
    @Autowired
    private TrainCacheService trainCacheService;
    @Autowired
    private TrainNumberService trainNumberService;

    public List<TrainNumberLeftDto> searchLeftCount(SearchLeftCountParam param) throws Exception{
        BeanValidator.check(param);
        List<TrainNumberLeftDto> dtoList = Lists.newArrayList();
        //先根据出发站点和到达站点id取ES中的车次信息
        //因为这里只需涉及到一次请求，所以不需要MultiGetRequest,使用GetRequest即可
        GetRequest getRequest = new GetRequest(TrainEsConstant.INDEX,TrainEsConstant.TYPE,param.getFromStationId() +"_"+ param.getToStationId());
        GetResponse getResponse = esClient.get(getRequest);
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        //当根据出发站点和到达站点 无法在ES中获取到数据时，直接返回空数据即可
        if(CollectionUtils.isEmpty(sourceAsMap)){
            return dtoList;
        }
        //取出对应的出发站点 ->到达站点的车次信息，因在ES中存储的是D123,D456这种格式，所以需要进行拆分
        String trainNumbers = (String) sourceAsMap.get(TrainEsConstant.COLUMN_TRAIN_NUMBER);
        List<String> trainNumberList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(trainNumbers);
        trainNumberList.parallelStream().forEach(number -> {
            TrainNumber trainNumber = trainNumberService.findByNameFromCache(number);
            if (trainNumber == null) {
                return;
            }
            String detailStr = trainCacheService.get("TN_" + number);
            List<TrainNumberDetail> detailList = JsonMapper.string2Obj(detailStr, new TypeReference<List<TrainNumberDetail>>() {
            });
            //这里将车次详情数据转成Map结构，key -> fromStationId , value -> detail
            Map<Integer,TrainNumberDetail> detailMap = Maps.newHashMap();
            detailList.stream().forEach(detail-> detailMap.put(detail.getFromStationId(),detail));
            /** 这个Map的作用如下：
             * 根据前台的传参 出发站 到  到达站点时，依次查询出从出发站开始 一直查询到toStationId为到达站点停止，
             * 且根据这些车断 查询若干个车断中间的座位余票，并取出余票数最小的数
             * 例如：2 ->{2(fromStationId),3(toStationId)}, 3 -> {3,4} -> 4 {4,5}
             * 当前台查询 2 - 5的车次信息 及余票时，需要查询 {2,3},{3,4},{4,5}车断中的座位余票，并取出三个车断中余票数最少的余票数展示
             * 为什么要取最小的呢？
             * 假如{2,3}:剩余5张余票 ,{3,4}:剩余3张余票, {4,5}:剩余10张余票
             * 在2到5中所有车断中的通用座位就只有三个，假如展示余票为5，2-5这个车次中最多也只能卖3张。
             */
            int fromStationId = param.getFromStationId();
            int targetToStationId = param.getToStationId();
            long min = Long.MAX_VALUE;
            Boolean isSuccess = Boolean.FALSE;
            String redisKey = trainNumber.getName()+"_"+param.getDate()+"_count";
            while(true){
                TrainNumberDetail trainNumberDetail = detailMap.get(fromStationId);
                if (trainNumberDetail == null) {
                    log.error("detail is null, stationId:{}, number:{}", fromStationId, number);
                    break;
                }
                //根据车次及时间信息、fromStationId和toStationId去redis中查询对应车次的座位余票
                min = Math.min(min,NumberUtils.toLong(trainCacheService.hget(redisKey,trainNumberDetail.getFromStationId()+"_"+trainNumberDetail.getToStationId()),00));
                if (trainNumberDetail.getToStationId() == targetToStationId) {
                    isSuccess = Boolean.TRUE;
                    break;
                }
                fromStationId = trainNumberDetail.getToStationId();
            }
            if(isSuccess){
                TrainNumberLeftDto trainNumberLeftDto = new TrainNumberLeftDto();
                trainNumberLeftDto.setId(trainNumber.getId());
                trainNumberLeftDto.setNumber(number);
                trainNumberLeftDto.setLeftCount(min);
                dtoList.add(trainNumberLeftDto);
            }
        });
        return dtoList;
    }
}
