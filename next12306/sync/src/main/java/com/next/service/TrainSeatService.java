package com.next.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.next.common.SeatStatusEnum;
import com.next.dao.TrainNumberDetailMapper;
import com.next.dao.TrainNumberMapper;
import com.next.model.TrainNumber;
import com.next.model.TrainSeat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: TrainSeatService
 * @Description:
 * @author: tjx
 * @date :2022/9/28 18:02
 */
@Service
@Slf4j
public class TrainSeatService {

    @Autowired
    private TrainNumberMapper trainNumberMapper;
    @Autowired
    private TrainCacheService trainCacheService;

    public void handle(List<CanalEntry.Column> columns, CanalEntry.EventType eventType){
        if(eventType != CanalEntry.EventType.UPDATE){
            log.info("not update, no need care ");
            return;
        }
        TrainSeat trainSeat = new TrainSeat();
        Boolean isStatusUpdated = Boolean.FALSE;
        for (CanalEntry.Column column : columns) {
            //这里之关系座位状态是否发生变更，否则直接退出循环
            if(column.getName().equals("status")){
                trainSeat.setStatus(Integer.valueOf(column.getValue()));
                if(column.getUpdated()){
                    isStatusUpdated = Boolean.TRUE;
                }else {
                    break;
                }
            } else if (column.getName().equals("id")) {
                trainSeat.setId(Long.parseLong(column.getValue()));
            } else if (column.getName().equals("carriage_number")) {
                trainSeat.setCarriageNumber(Integer.parseInt(column.getValue()));
            } else if (column.getName().equals("row_number")) {
                trainSeat.setRowNumber(Integer.parseInt(column.getValue()));
            } else if (column.getName().equals("seat_number")) {
                trainSeat.setSeatNumber(Integer.parseInt(column.getValue()));
            } else if (column.getName().equals("train_number_id")) {
                trainSeat.setTrainNumberId(Integer.parseInt(column.getValue()));
            } else if (column.getName().equals("id")) {
                trainSeat.setId(Long.parseLong(column.getValue()));
            } else if (column.getName().equals("ticket")) {
                trainSeat.setTicket(column.getValue());
            } else if (column.getName().equals("from_station_id")) {
                trainSeat.setFromStationId(Integer.parseInt(column.getValue()));
            } else if (column.getName().equals("to_station_id")) {
                trainSeat.setToStationId(Integer.parseInt(column.getValue()));
            }
        }
        if(!isStatusUpdated){
            log.info("status not update, no need care");
            return;
        }
        //获取到状态受更改的座位信息
        log.info("train seat status update, trainSeat:{}", trainSeat);

        /** 将座位状态保存到redis中 拿到座位数据之后需要做以下两个事情
         * 1、座位是否被占座
         *  这里两个逻辑使用Redis hash数据结构来进行存储
         *  hash key1：车次_时间 ，key2：车厢_排_座位号_fromStationId_toStationId，value 0：表示该座位未占座，1：表示该座位已占座
         *
         * 2、查询某两个车站之间的余票
         *  hash key1: 车次_时间_count key2: fromStationId_toStationId value: 余票数
         */
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(trainSeat.getTrainNumberId());
        if (trainSeat.getStatus().intValue() == SeatStatusEnum.PUT_TICKET.getStatus()) { //放票
            trainCacheService.hset(
                    trainNumber.getName()+"_"+trainSeat.getTicket(),
                    trainSeat.getCarriageNumber()+"_"+trainSeat.getRowNumber()+"_"+trainSeat.getSeatNumber()+"_"+trainSeat.getFromStationId()+"_"+trainSeat.getToStationId(),
                    "0"
            );
            trainCacheService.hincrBy(
                    trainNumber.getName()+"_"+trainSeat.getTicket()+"_count",
                    trainSeat.getFromStationId()+"_"+trainSeat.getToStationId(),
                    1l
            );
            log.info("seat + 1, trainNumber : {}, trainSeat : {}",trainNumber,trainSeat);
        }else if(trainSeat.getStatus().intValue() == SeatStatusEnum.SCRAMBLE_TICKET.getStatus()){ //占座
            trainCacheService.hset(
                    trainNumber.getName()+"_"+trainSeat.getTicket(),
                    trainSeat.getCarriageNumber()+"_"+trainSeat.getRowNumber()+"_"+trainSeat.getSeatNumber()+"_"+trainSeat.getFromStationId()+"_"+trainSeat.getToStationId(),
                    "1"
            );
            trainCacheService.hincrBy(
                    trainNumber.getName()+"_"+trainSeat.getTicket()+"_count",
                    trainSeat.getFromStationId()+"_"+trainSeat.getToStationId(),
                    -1l
            );
            log.info("seat - 1, trainNumber : {}, trainSeat : {}",trainNumber,trainSeat);
        }else{
            log.info("status no 1 or 2, no need care");
        }
    }
}
