package com.next.mq;

import com.next.dto.RollbackSeatDto;
import com.next.model.TrainOrder;
import com.next.service.TrainOrderService;
import com.next.service.TrainSeatService;
import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Title: RabbitDelayMqServer
 * @Description:
 * @author: tjx
 * @date :2022/10/4 21:24
 */
@Component
@Slf4j
public class RabbitDelayMqServer {

    @Autowired
    private TrainSeatService trainSeatService;
    @Autowired
    private TrainOrderService trainOrderService;

    @RabbitListener(queues = QueueConstants.DELAY_QUEUE)
    public void receive(String message){
        log.info("delay queue receive msg :{}",message);

        try {
            MessageBody messageBody = JsonMapper.string2Obj(message, new TypeReference<MessageBody>() {});
            if (messageBody == null) {
                return;
            }
            switch (messageBody.getTopic()){
                case 1: //处理回滚占座的消息逻辑
                    RollbackSeatDto dto = JsonMapper.string2Obj(messageBody.getDetail(), new TypeReference<RollbackSeatDto>() {});
                    //继续调用回滚占座DB操作
                    trainSeatService.batchRollbackSeat(dto.getTrainSeat(),dto.getFromStationIdList(), messageBody.getDelay());
                    break;
                case 3: //订单延迟支付操作
                    TrainOrder trainOrder = JsonMapper.string2Obj(messageBody.getDetail(), new TypeReference<TrainOrder>() {});
                    trainOrderService.delayCheckOrder(trainOrder);
                    break;
                default:
                    log.warn("delay queue receive msg ,{} no need handle",message);
            }
        } catch (Exception e) {
            log.error("delay queue receive msg exception, msg: {}",message);
        }
    }
}
