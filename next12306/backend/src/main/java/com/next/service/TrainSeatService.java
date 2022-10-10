package com.next.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.next.beans.PageQuery;
import com.next.common.TrainSeatLevel;
import com.next.common.TrainType;
import com.next.common.TrainTypeSeatConstant;
import com.next.dao.TrainNumberDetailMapper;
import com.next.dao.TrainNumberMapper;
import com.next.exception.BusinessException;
import com.next.model.TrainNumber;
import com.next.model.TrainNumberDetail;
import com.next.model.TrainSeat;
import com.next.param.GenerateTicketParam;
import com.next.param.PublishTicketParam;
import com.next.param.TrainSeatSearchParam;
import com.next.seatDao.TrainSeatMapper;
import com.next.util.BeanValidator;
import com.next.util.StrUtil;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Title: TrainSeatService
 * @Description:
 * @author: tjx
 * @date :2022/9/26 21:29
 */
@Service
public class TrainSeatService {

    @Autowired
    private TrainNumberMapper trainNumberMapper;
    @Autowired
    private TrainNumberDetailMapper trainNumberDetailMapper;
    @Autowired
    private TrainSeatMapper trainSeatMapper;
    @Autowired
    private TrainSeatService trainSeatService;

    public List<TrainSeat> searchList(TrainSeatSearchParam param, PageQuery pageQuery){
        BeanValidator.check(param);
        BeanValidator.check(pageQuery);
        TrainNumber trainNumber = trainNumberMapper.findByName(param.getTrainNumber());
        if (trainNumber == null) {
            throw new BusinessException("待查询的车次不存在");
        }
       return trainSeatMapper.searchList(trainNumber.getId(), param.getTicket(), param.getCarriageNum(), param.getRowNum(), param.getSeatNum(),
                                  param.getStatus(),pageQuery.getOffSet(),pageQuery.getPageSize());
    }

    public Integer countList(TrainSeatSearchParam param){
        TrainNumber trainNumber = trainNumberMapper.findByName(param.getTrainNumber());
        if (trainNumber == null) {
            throw new BusinessException("待查询的车次不存在");
        }
        return trainSeatMapper.countList(trainNumber.getId(), param.getTicket(), param.getCarriageNum(), param.getRowNum(), param.getSeatNum(),
                                         param.getStatus());
    }

    public void generate(GenerateTicketParam param){
        BeanValidator.check(param);
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(param.getTrainNumberId());
        if (trainNumber == null) {
            throw new BusinessException("车次不存在");
        }
        List<TrainNumberDetail> trainNumberDetailList = trainNumberDetailMapper.getByTrainNumberId(param.getTrainNumberId());
        if (CollectionUtils.isEmpty(trainNumberDetailList)) {
            throw new BusinessException("该车次暂无详情，需先添加详情");
        }
        //获取座位类型
        TrainType trainType = TrainType.valueOf(trainNumber.getTrainType());
        //根据车次类型 获取该车次类型的实际座位数据
        Table<Integer, Integer, Pair<Integer, Integer>> seatTable = TrainTypeSeatConstant.getTable(trainType);

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime formLocalDateTime = LocalDateTime.parse(param.getFromTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        List<TrainSeat> trainSeatList = Lists.newArrayList();
        //标识发车的日期
        String ticket = formLocalDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (TrainNumberDetail trainNumberDetail : trainNumberDetailList) { //遍历车次详情的每一段
            //每一段发车时间
            Date formDate = Date.from(formLocalDateTime.atZone(zoneId).toInstant());
            //每一段到站时间
            Date toDate = Date.from(formLocalDateTime.plusMinutes(trainNumberDetail.getRelativeMinute()).atZone(zoneId).toInstant());
            Map<Integer,Integer> seatMoneyMap = seatMoneyMap(trainNumberDetail.getMoney());
            for (Table.Cell<Integer, Integer, Pair<Integer, Integer>> cell : seatTable.cellSet()) { //遍历每一节车厢的每一排
                Integer carriage = cell.getRowKey(); //当前遍历的车厢
                Integer row = cell.getColumnKey(); //当前遍历的车厢的排
                TrainSeatLevel seatLevel = TrainTypeSeatConstant.getSeatLevel(trainType, carriage);//获取座位等级
                Integer money = seatMoneyMap.get(seatLevel.getLevel()); //根据座位等级获取对应的价钱
                Pair<Integer,Integer> rowSeatRange = seatTable.get(carriage,row);//获取具体每一排的座位号范围
                for (int i = rowSeatRange.getKey(); i <= rowSeatRange.getValue() ; i++) {
                    //生成座位
                    TrainSeat trainSeat = TrainSeat.builder()
                                                    .trainNumberId(trainNumber.getId())
                                                    .carriageNumber(carriage)
                                                    .rowNumber(row)
                                                    .seatNumber(i)
                                                    .showNumber(carriage + "车" + row + "排" + i)
                                                    .money(money)
                                                    .trainStart(formDate)
                                                    .trainEnd(toDate)
                                                    .ticket(ticket)
                                                    .status(0)
                                                    .seatLevel(seatLevel.getLevel())
                                                    .fromStationId(trainNumberDetail.getFromStationId())
                                                    .toStationId(trainNumberDetail.getToStationId())
                                                    .build();
                    trainSeatList.add(trainSeat);
                }
            }
            formLocalDateTime = formLocalDateTime.plusMinutes(trainNumberDetail.getRelativeMinute() + trainNumberDetail.getWaitMinute());
        }
        //注意：这里通过自己注入自己的方式 调用batchInsertSeat避免事务失效，如果直接调用batchInsertSeat方法事务会失效
        // 在spring事务中调用batchInsertSeat方法是通过代理对象去执行，而不是当前对象
        trainSeatService.batchInsertSeat(trainSeatList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchInsertSeat(List<TrainSeat> trainSeatList) {
        List<List<TrainSeat>> trainTicketPartitionList = Lists.partition(trainSeatList,1000);
        trainTicketPartitionList.parallelStream().forEach(partitionList -> {
            //批量插入
            trainSeatMapper.batchInsert(partitionList);
        });
    }

    private Map<Integer,Integer> seatMoneyMap(String money){
        try{
            String[] moneys = StringUtils.delimitedListToStringArray(money, ",");
            Map<Integer,Integer> moneyMap = Maps.newHashMap();
            for (String seatLevelMoney : moneys) {
                String[] seatLevelMoneyArray = seatLevelMoney.split(":");
                moneyMap.put(Integer.valueOf(seatLevelMoneyArray[0]),Integer.valueOf(seatLevelMoneyArray[1]));
            }
            return moneyMap;
        }catch (Exception e){
            throw new BusinessException("价钱解析错误");
        }
    }

    public void publish(PublishTicketParam param){
        BeanValidator.check(param);
        //获取需要放票的车次信息
        TrainNumber trainNumber = trainNumberMapper.findByName(param.getTrainNumber());
        if (trainNumber == null) {
            throw new BusinessException("待放票的车次不存在");
        }
        //获取车次中需要放票的座位信息
        List<Long> seatIds = StrUtil.splitToListLong(param.getTrainSeatIds());
        List<List<Long>> seatPartitionList = Lists.partition(seatIds,1000);
        seatPartitionList.stream().forEach(partitionList ->{
            Integer row = trainSeatMapper.batchPublish(trainNumber.getId(), partitionList);
            //这里将修改返回的受影响行与集合中的id数据大小做比较，如果两值不相等，则抛出业务异常
            if(row != seatIds.size()){
                throw new BusinessException("部分座位不满足更改放票需求，请重新核对座位状态为[初始]的数据");
            }
        });

    }
}
