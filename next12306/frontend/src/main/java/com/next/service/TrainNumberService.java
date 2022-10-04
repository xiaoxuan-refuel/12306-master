package com.next.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.next.dao.TrainNumberMapper;
import com.next.model.TrainNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Title: TrainNumberService
 * @Description: 将查询车次的操作做为一个本地内存缓存，过期时间为5分钟
 * @author: tjx
 * @date :2022/10/3 17:52
 */
@Service
public class TrainNumberService {
    private static Cache<String,TrainNumber> trainNumberCache = CacheBuilder.newBuilder()
                                                                            .expireAfterWrite(5, TimeUnit.MINUTES)
                                                                            .build();

    @Autowired
    private TrainNumberMapper trainNumberMapper;


    public TrainNumber findByNameFromCache(String name){
        TrainNumber trainNumber = trainNumberCache.getIfPresent(name);
        if (trainNumber != null) {
            return trainNumber;
        }
        trainNumber = trainNumberMapper.findByName(name);
        if (trainNumber != null) {
            trainNumberCache.put(name,trainNumber);
        }
        return trainNumber;
    }
}
