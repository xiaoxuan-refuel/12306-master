package com.next.dao;

import com.next.model.TrainNumberDetail;
import org.apache.ibatis.annotations.Mapper;

public interface TrainNumberDetailMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TrainNumberDetail record);

    int insertSelective(TrainNumberDetail record);

    TrainNumberDetail selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TrainNumberDetail record);

    int updateByPrimaryKey(TrainNumberDetail record);
}