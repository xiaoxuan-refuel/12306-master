package com.next.service;

import com.next.dao.TrainStationMapper;
import com.next.model.TrainStation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: TrainStationService
 * @Description:
 * @author: tjx
 * @date :2022/10/3 15:29
 */
@Service
@Slf4j
public class TrainStationService {

    @Autowired
    private TrainStationMapper trainStationMapper;

    public List<TrainStation> getAll(){
        return trainStationMapper.getAll();
    }
}
