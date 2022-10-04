package com.next.dao;

import com.next.model.TrainTraveller;
import com.next.model.TrainUserTraveller;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TrainUserTravellerMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TrainUserTraveller record);

    int insertSelective(TrainUserTraveller record);

    TrainUserTraveller selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrainUserTraveller record);

    int updateByPrimaryKey(TrainUserTraveller record);

    List<Long> getByUserId(@Param("userId") Long userId);


}