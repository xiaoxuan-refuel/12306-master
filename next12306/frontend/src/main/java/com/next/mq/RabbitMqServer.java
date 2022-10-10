package com.next.mq;

import com.google.common.collect.Lists;
import com.next.dto.RollbackSeatDto;
import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import com.next.orderDao.TrainOrderDetailMapper;
import com.next.seatDao.TrainSeatMapper;
import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Title: RabbitMqServer
 * @Description:
 * @author: tjx
 * @date :2022/10/4 21:23
 */
@Component
@Slf4j
public class RabbitMqServer {

    @Autowired
    private TrainOrderDetailMapper trainOrderDetailMapper;
    @Autowired
    private TrainSeatMapper trainSeatMapper;

    @RabbitListener(queues = QueueConstants.COMMON_QUEUE)
    public void receive(String message){
        log.info("send receive msg :{}",message);
        try {
            MessageBody messageBody = JsonMapper.string2Obj(message, new TypeReference<MessageBody>() {});
            if (messageBody == null) {
                return;
            }
            switch (messageBody.getTopic()){
                case 2: //订单创建之后需要做的事情(例如：发短信、邮件等)
                    log.info("create order success ,order:{}",messageBody.getDetail());
                    break;
                case 4: //订单取消具体处理逻辑
                    log.info("cancel order message , msg:{}",message);
                    TrainOrder trainOrder = JsonMapper.string2Obj(message, new TypeReference<TrainOrder>() {});
                    //根据主订单号查询具体的车次详情信息
                    List<TrainOrderDetail> trainOrderDetailList = trainOrderDetailMapper.getByParentOrderId(Lists.newArrayList(trainOrder.getOrderId()));
                    for (TrainOrderDetail trainOrderDetail : trainOrderDetailList) {
                        //这里根据车次详情中的车次信息、时间、车厢号、排、座位号、以及根据乘客id、用户进行一个乐观锁检查，找到具体更新的位置
                        trainSeatMapper.cancelSeat(trainOrderDetail.getTrainNumberId(),trainOrderDetail.getTicket(),
                                                   trainOrderDetail.getCarriageNumber(),trainOrderDetail.getRowNumber(),
                                                   trainOrderDetail.getSeatNumber(),trainOrderDetail.getTravellerId(),
                                                   trainOrderDetail.getUserId());
                    }
                    log.info("order cancel seat success , message:{}",message);
                    break;
                case 5: //支付成功之后处理的逻辑
                    log.info("order pay success,  message , msg:{}",message);
                    break;
                default:
                    log.warn("common queue receive msg ,{} no need handle",message);
            }
        } catch (Exception e) {
            log.error("common queue receive msg exception, msg: {}",message);
        }
    }
}
