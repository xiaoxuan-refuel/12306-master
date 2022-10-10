package com.next.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.next.dao.TrainStationMapper;
import com.next.model.TrainStation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Title: TrainStationService
 * @Description:
 * @author: tjx
 * @date :2022/10/3 15:29
 */
@Service
@Slf4j
public class TrainStationService {

    private static Cache<Integer,TrainStation> trainStationCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    @Autowired
    private TrainStationMapper trainStationMapper;

    public List<TrainStation> getAll(){
        return trainStationMapper.getAll();
    }

    public String getNameById(Integer stationId){
        TrainStation trainStation = trainStationCache.getIfPresent(stationId);
        if (trainStation != null) {
            return trainStation.getName();
        }
        trainStation = trainStationMapper.selectByPrimaryKey(stationId);
        if (trainStation != null) {
            trainStationCache.put(stationId,trainStation);
            return trainStation.getName();
        }
        return "";
    }
}
