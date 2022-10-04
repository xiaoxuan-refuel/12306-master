package com.next.service;

import com.google.common.collect.Lists;
import com.next.dao.TrainTravellerMapper;
import com.next.dao.TrainUserMapper;
import com.next.dao.TrainUserTravellerMapper;
import com.next.model.TrainTraveller;
import com.next.model.TrainUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: TrainTravellerService
 * @Description:
 * @author: tjx
 * @date :2022/10/4 13:42
 */
@Service
@Slf4j
public class TrainTravellerService {

    @Autowired
    private TrainTravellerMapper trainTravellerMapper;
    @Autowired
    private TrainUserTravellerMapper trainUserTravellerMapper;


    public List<TrainTraveller> getByUserId(Long userId){
        List<Long> travelleridsIdList = trainUserTravellerMapper.getByUserId(userId);
        if (CollectionUtils.isEmpty(travelleridsIdList)) {
            return Lists.newArrayList();
        }
        return trainTravellerMapper.getByIdList(travelleridsIdList);
    }
}
