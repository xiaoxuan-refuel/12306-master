package com.next.service;

import com.next.common.OrderStatusEnum;
import com.next.exception.BusinessException;
import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import com.next.model.TrainUser;
import com.next.mq.MessageBody;
import com.next.mq.QueueTopic;
import com.next.mq.RabbitMqClient;
import com.next.orderDao.TrainOrderDetailMapper;
import com.next.orderDao.TrainOrderMapper;
import com.next.param.CancelOrderParam;
import com.next.param.PayOrderParam;
import com.next.seatDao.TrainSeatMapper;
import com.next.util.BeanValidator;
import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Title: TrainOrderService
 * @Description:
 * @author: tjx
 * @date :2022/10/10 10:14
 */
@Service
@Slf4j
public class TrainOrderService {
    @Autowired
    private TrainOrderMapper trainOrderMapper;
    @Autowired
    private TrainOrderDetailMapper trainOrderDetailMapper;
    @Autowired
    private RabbitMqClient rabbitMqClient;

    public void delayCheckOrder(TrainOrder trainOrder){
        log.info("delay check order : {}",trainOrder);
        TrainOrder order = trainOrderMapper.getByOrderId(trainOrder.getOrderId());
        if (order == null) {
            log.error("order is null , order: {}",trainOrder);
            return;
        }
        //如果当前订单状态仍是为未支付状态，则强制将订单状态改为取消
        if(order.getStatus() == OrderStatusEnum.AWAIT_PAY.getStatus()){
            log.info("order no payment ,force cancel, order :{}",order);
            order.setStatus(OrderStatusEnum.TIME_OUT_NON_PAY.getStatus());
            trainOrderMapper.updateByPrimaryKeySelective(order);
            //发送取消订单的实施消息，让实施消息具体处理
            MessageBody messageBody = new MessageBody();
            messageBody.setTopic(QueueTopic.ORDER_CANCEL);
            messageBody.setDetail(JsonMapper.obj2String(order));
            rabbitMqClient.send(messageBody);
        }
    }


    public List<TrainOrder> getOrderList(long userId){
        return trainOrderMapper.getByUserId(userId);
    }

    public List<TrainOrderDetail> getOrderDetailList(List<String> orderIds){
        return trainOrderDetailMapper.getByParentOrderId(orderIds);
    }

    public void payOrder(PayOrderParam param, Long userId){
        BeanValidator.check(param);
        TrainOrder trainOrder = trainOrderMapper.getByOrderId(param.getOrderId());
        if (trainOrder == null) {
            throw new BusinessException("订单不存在");
        }
        if (trainOrder.getUserId() != userId) {
            throw new BusinessException("不能操作其他人的订单");
        }
        if (trainOrder.getStatus() != OrderStatusEnum.AWAIT_PAY.getStatus()) {
            throw new BusinessException("订单状态有误，请刷新重试");
        }
        trainOrder.setStatus(OrderStatusEnum.HAVE_PAID.getStatus());
        trainOrderMapper.updateByPrimaryKeySelective(trainOrder);
        MessageBody messageBody = new MessageBody();
        messageBody.setTopic(QueueTopic.ORDER_PAY_SUCCESS);
        messageBody.setDetail(JsonMapper.obj2String(trainOrder));
        rabbitMqClient.send(messageBody);
    }

    public void cancelOrder(CancelOrderParam param,Long userId){
        BeanValidator.check(param);
        TrainOrder trainOrder = trainOrderMapper.getByOrderId(param.getOrderId());
        if (trainOrder == null) {
            throw new BusinessException("订单不存在");
        }
        if (trainOrder.getUserId() != userId) {
            throw new BusinessException("不能操作其他人的订单");
        }
        if (trainOrder.getStatus() != OrderStatusEnum.HAVE_PAID.getStatus()) {
            throw new BusinessException("订单状态有误，请刷新重试");
        }
//        if (trainOrder.getTrainStart().before(new Date())){
//            throw new BusinessException("当前时间超过始发时间，无法取消状态");
//        }
        trainOrder.setStatus(OrderStatusEnum.HAVE_PAID_REFUND.getStatus());
        trainOrderMapper.updateByPrimaryKeySelective(trainOrder);
        MessageBody messageBody = new MessageBody();
        messageBody.setTopic(QueueTopic.ORDER_CANCEL);
        messageBody.setDetail(JsonMapper.obj2String(trainOrder));
        rabbitMqClient.send(messageBody);
    }
}
