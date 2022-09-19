package com.next.dao;

import com.next.model.TrainStation;
import org.apache.ibatis.annotations.Mapper;

public interface TrainStationMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TrainStation record);

    int insertSelective(TrainStation record);

    TrainStation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TrainStation record);

    int updateByPrimaryKey(TrainStation record);
}