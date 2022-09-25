package com.next.service;

import com.next.dao.TrainNumberDetailMapper;
import com.next.dao.TrainNumberMapper;
import com.next.exception.BusinessException;
import com.next.model.TrainNumber;
import com.next.model.TrainNumberDetail;
import com.next.param.TrainNumberDetailParam;
import com.next.util.BeanValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: TrainNumberDetailService
 * @Description:
 * @author: tjx
 * @date :2022/9/22 22:45
 */
@Service
public class TrainNumberDetailService {
    @Autowired
    private TrainNumberDetailMapper trainNumberDetailMapper;

    @Autowired
    private TrainNumberMapper trainNumberMapper;

    @Autowired
    private TrainStationService trainStationService;

    public List<TrainNumberDetail> getAll(){
        return trainNumberDetailMapper.getAll();
    }

    public void save(TrainNumberDetailParam detailParam){
        BeanValidator.check(detailParam);
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(detailParam.getTrainNumberId());
        if (trainNumber == null) {
            throw new BusinessException("车次不存在");
        }
        List<TrainNumberDetail> trainNumberDetailList = trainNumberDetailMapper.getByTrainNumberId(detailParam.getTrainNumberId());
        TrainNumberDetail trainNumberDetail = TrainNumberDetail.builder().trainNumberId(detailParam.getTrainNumberId())
                                                                        .fromStationId(detailParam.getFromStationId())
                                                                        .toStationId(detailParam.getToStationId())
                                                                        .relativeMinute(detailParam.getRelativeMinute())
                                                                        .stationIndex(trainNumberDetailList.size())
                                                                        .waitMinute(detailParam.getWaitMinute())
                                                                        .fromCityId(trainStationService.getCityIdByStationId(detailParam.getFromStationId()))
                                                                        .toCityId(trainStationService.getCityIdByStationId(detailParam.getToStationId()))
                                                                        .money(detailParam.getMoney()).build();
        trainNumberDetailMapper.insertSelective(trainNumberDetail);
        trainNumberDetailList.add(trainNumberDetail);
        if(detailParam.getEnd() == 1){ //为1代表是车次中最后一次详情信息，也代表该车次详情已添加完成
               //当车次详情已添加完成时，取排序之后详情集合中的第一个车站id，车站所属城市id作为始发站，取最后一个车站id、车站所属城市id作为终点站
               trainNumber.setFromCityId(trainNumberDetailList.get(0).getFromCityId());
               trainNumber.setFromStationId(trainNumberDetailList.get(0).getFromStationId());
               trainNumber.setToCityId(trainNumberDetailList.get(trainNumberDetailList.size() - 1 ).getToCityId());
               trainNumber.setToStationId(trainNumberDetailList.get(trainNumberDetailList.size() - 1 ).getToStationId());
               trainNumberMapper.updateByPrimaryKeySelective(trainNumber);

               //TODO 这里需要考虑前台用户根据始发站到终点站查询出不同的车次信息，在实际过程中同一个始发站到终点站肯定不只一个车次信息的
        }

    }

    public void delete(Integer id){
        trainNumberDetailMapper.deleteByPrimaryKey(id);
    }
}
