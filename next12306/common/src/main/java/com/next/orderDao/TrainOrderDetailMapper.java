package com.next.orderDao;

import com.next.model.TrainOrderDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TrainOrderDetailMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TrainOrderDetail record);

    int insertSelective(TrainOrderDetail record);

    TrainOrderDetail selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrainOrderDetail record);

    int updateByPrimaryKey(TrainOrderDetail record);

    List<TrainOrderDetail> getByParentOrderId(@Param("parentOrderIdList") List<String> parentOrderIdList);

}