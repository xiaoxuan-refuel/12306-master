package com.next.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Title: RabbitMqServer
 * @Description:
 * @author: tjx
 * @date :2022/10/4 21:23
 */
@Component
@Slf4j
public class RabbitMqServer {

    @RabbitListener(queues = QueueConstants.COMMON_QUEUE)
    public void receive(String message){
        log.info("send receive msg :{}",message);
    }
}
