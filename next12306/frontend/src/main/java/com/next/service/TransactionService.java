package com.next.service;

import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import com.next.orderDao.TrainOrderDetailMapper;
import com.next.orderDao.TrainOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Title: TransactionService
 * @Description: 创建一个事务服务，将使用事务的方法放到这个类中，防止事务失效的问题
 * 防止这种事务的问题还可以使用自己注入自己的方式去避免，在后台backend模块中座位生成中(TrainSeatService.batchInsertSeat)使用的这种方法
 * 且事务的方法必须是被public修饰
 * @author: tjx
 * @date :2022/10/9 16:16
 */
@Service
public class TransactionService {

    @Autowired
    private TrainOrderMapper trainOrderMapper;
    @Autowired
    private TrainOrderDetailMapper trainOrderDetailMapper;

    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(TrainOrder trainOrder, List<TrainOrderDetail> trainOrderDetailList){
        for (TrainOrderDetail trainOrderDetail : trainOrderDetailList) {
            trainOrderDetailMapper.insertSelective(trainOrderDetail);
        }
        trainOrderMapper.insertSelective(trainOrder);
    }
}
