package com.next.dao;

import com.next.model.TrainUser;
import org.apache.ibatis.annotations.Mapper;

public interface TrainUserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TrainUser record);

    int insertSelective(TrainUser record);

    TrainUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrainUser record);

    int updateByPrimaryKey(TrainUser record);
}