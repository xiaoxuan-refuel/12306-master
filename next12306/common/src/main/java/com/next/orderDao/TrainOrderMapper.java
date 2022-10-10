package com.next.orderDao;

import com.next.model.TrainOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TrainOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TrainOrder record);

    int insertSelective(TrainOrder record);

    TrainOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrainOrder record);

    int updateByPrimaryKey(TrainOrder record);

    TrainOrder getByOrderId(@Param("orderId") String orderId);

    List<TrainOrder> getByUserId(@Param("userId") Long userId);
}