package com.next.dao;

import com.next.model.TrainNumber;
import com.next.model.TrainStation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TrainNumberMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TrainNumber record);

    int insertSelective(TrainNumber record);

    TrainNumber selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TrainNumber record);

    int updateByPrimaryKey(TrainNumber record);

    List<TrainNumber> getAll();

    TrainNumber findByName(@Param("name") String name);
}