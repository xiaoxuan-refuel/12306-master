package com.next.dao;

import com.next.model.TrainNumberDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TrainNumberDetailMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TrainNumberDetail record);

    int insertSelective(TrainNumberDetail record);

    TrainNumberDetail selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TrainNumberDetail record);

    int updateByPrimaryKey(TrainNumberDetail record);

    List<TrainNumberDetail> getAll();

    List<TrainNumberDetail> getByTrainNumberId(@Param("trainNumberId") Integer trainNumberId);
}