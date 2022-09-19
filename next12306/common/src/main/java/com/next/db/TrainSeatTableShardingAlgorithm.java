package com.next.db;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

import java.util.Collection;

/**
 * @Title: TrainSeatTableShardingAlgorithm
 * @Description: 分表策略
 * @author: tjx
 * @date :2022/9/19 21:16
 */
public class TrainSeatTableShardingAlgorithm implements PreciseShardingAlgorithm<Integer> {
    private static final String PREFIX = "train_seat_";

    private String determineTable(int val){
        if(val % 10 == 0){
            val = 10;
        }
        return PREFIX + val;
    }


    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Integer> preciseShardingValue) {
        String determineTable = determineTable(preciseShardingValue.getValue());
        if (collection.contains(determineTable)) {
            return determineTable;
        }
        throw new IllegalArgumentException();
    }
}
