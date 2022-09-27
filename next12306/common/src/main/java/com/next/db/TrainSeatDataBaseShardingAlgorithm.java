package com.next.db;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

import java.util.Collection;

/**
 * @Title: TrainSeatDataBaseShardingAlgorithm
 * @Description: 分库策略
 * @author: tjx
 * @date :2022/9/19 21:12
 */
public class TrainSeatDataBaseShardingAlgorithm implements PreciseShardingAlgorithm<Integer> {
    private static final String PREFIX = "trainSeatDB";

    private String determineDB(int val){
        //val取余5值为1、2、3、4的库直接落到对应的库中，当取余5为0时直接将库落到库5中
        int db = val % 5;
        if(db == 0){
            db = 5;
        }
        return PREFIX + db;
    }

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Integer> preciseShardingValue) {
        String determineDB = determineDB(preciseShardingValue.getValue());
        if (collection.contains(determineDB)) {
            return determineDB;
        }
        throw new IllegalArgumentException();
    }
}
