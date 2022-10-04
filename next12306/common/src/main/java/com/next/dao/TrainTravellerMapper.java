package com.next.dao;

import com.next.model.TrainTraveller;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TrainTravellerMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TrainTraveller record);

    int insertSelective(TrainTraveller record);

    TrainTraveller selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrainTraveller record);

    int updateByPrimaryKey(TrainTraveller record);

    List<TrainTraveller> getByIdList(@Param("idList") List<Long> idList);
}