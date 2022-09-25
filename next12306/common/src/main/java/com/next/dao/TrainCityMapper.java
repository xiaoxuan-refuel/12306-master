package com.next.dao;

import com.next.model.TrainCity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TrainCityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TrainCity record);

    int insertSelective(TrainCity record);

    TrainCity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TrainCity record);

    int updateByPrimaryKey(TrainCity record);

    List<TrainCity> getAll();

    Integer countByNameAndId(@Param("name") String name, @Param("trainCityId") Integer trainCityId);

}