package com.next.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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

    @RabbitListener(queues = QueueConstants.DELAY_QUEUE)
    public void receive(String message){
        log.info("send receive msg :{}",message);
    }
}
