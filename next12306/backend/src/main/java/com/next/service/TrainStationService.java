package com.next.service;

import com.next.dao.TrainCityMapper;
import com.next.dao.TrainStationMapper;
import com.next.exception.BusinessException;
import com.next.model.TrainCity;
import com.next.model.TrainStation;
import com.next.param.TrainStationParam;
import com.next.util.BeanValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: TrainStationService
 * @Description: 车站管理服务类
 * @author: tjx
 * @date :2022/9/22 22:45
 */
@Service
public class TrainStationService {

    @Autowired
    private TrainStationMapper trainStationMapper;

    @Autowired
    private TrainCityMapper trainCityMapper;

    public List<TrainStation> getAll(){
            return trainStationMapper.getAll();
    }

    public void save(TrainStationParam stationParam){
        BeanValidator.check(stationParam);
        TrainCity trainCity = trainCityMapper.selectByPrimaryKey(stationParam.getCityId());
        if (trainCity == null) {
            throw new BusinessException("站点所属城市不存在");
        }
        if(checkExist(stationParam.getName(),stationParam.getId(),stationParam.getCityId())){
            throw new BusinessException("该城市下存在相同的站点名称");
        }
        TrainStation trainStation = TrainStation.builder().cityId(stationParam.getCityId()).name(stationParam.getName()).build();
        trainStationMapper.insert(trainStation);
    }

    public void update(TrainStationParam stationParam){
        BeanValidator.check(stationParam);
        TrainCity trainCity = trainCityMapper.selectByPrimaryKey(stationParam.getCityId());
        if (trainCity == null) {
            throw new BusinessException("站点所属城市不存在");
        }
        if(checkExist(stationParam.getName(),stationParam.getId(),stationParam.getCityId())){
            throw new BusinessException("该城市下存在相同的站点名称");
        }
        TrainStation trainStation = trainStationMapper.selectByPrimaryKey(stationParam.getId());
        if (trainStation == null) {
            throw new BusinessException("需更新的站点不存在");
        }
        TrainStation trainStationBuilder = TrainStation.builder().id(stationParam.getId()).name(stationParam.getName()).cityId(stationParam.getCityId()).build();
        trainStationMapper.updateByPrimaryKeySelective(trainStationBuilder);
    }

    /**
     * 车站新增：name、cityId不为空，且需验证在同一个城市下是否有相同的站点名称
     * 车站修改：name、cityId、stationId不为空，需验证除当前stationId之外是否在同一个城市有相同的站点名称
     * @param name
     * @param stationId
     * @param cityId
     * @return
     */
    private Boolean checkExist(String name,Integer stationId,Integer cityId){
        return trainStationMapper.countByIdAndNameAndCityId(name,stationId,cityId) > 0;
    }

    public Integer getCityIdByStationId(Integer stationId){
        TrainStation trainStation = trainStationMapper.selectByPrimaryKey(stationId);
        if (trainStation == null) {
            throw new BusinessException("车站不存在");
        }
        return trainStation.getCityId();
    }
}
