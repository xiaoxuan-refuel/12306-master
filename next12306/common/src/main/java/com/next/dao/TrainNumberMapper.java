package com.next.dao;

import com.next.model.TrainNumber;
import org.apache.ibatis.annotations.Mapper;

public interface TrainNumberMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TrainNumber record);

    int insertSelective(TrainNumber record);

    TrainNumber selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TrainNumber record);

    int updateByPrimaryKey(TrainNumber record);
}