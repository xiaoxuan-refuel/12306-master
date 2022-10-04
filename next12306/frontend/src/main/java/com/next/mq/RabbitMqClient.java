package com.next.mq;

import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @Title: RabbitMqClient
 * @Description:
 * @author: tjx
 * @date :2022/10/4 21:16
 */
@Component
@Slf4j
public class RabbitMqClient {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(MessageBody messageBody){
        try {
            String uuid = UUID.randomUUID().toString();//消息唯一id
            CorrelationData correlationData = new CorrelationData(uuid);
            rabbitTemplate.convertAndSend(QueueConstants.COMMON_EXCHANGE, QueueConstants.COMMON_ROUTING,
                    JsonMapper.obj2String(messageBody), new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);//做消息持久化
                            log.info("message send, {}",message);
                            return message;
                        }
                    },correlationData);
        } catch (Exception e) {
            log.error("send message exception, msg:{}",messageBody,e);
        }
    }

    public void sendDelay(MessageBody messageBody,Integer delayMillSeconds){
        try {
            messageBody.setDelay(delayMillSeconds);
            String uuid = UUID.randomUUID().toString();//消息唯一id
            CorrelationData correlationData = new CorrelationData(uuid);
            rabbitTemplate.convertAndSend(QueueConstants.DELAY_EXCHANGE, QueueConstants.DELAY_ROUTING,
                    JsonMapper.obj2String(messageBody), new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);//做消息持久化
                            message.getMessageProperties().setDelay(delayMillSeconds);
                            log.info("delay message send, {}",message);
                            return message;
                        }
                    },correlationData);
        } catch (Exception e) {
            log.error("delay send message exception, msg:{}",messageBody,e);
        }
    }
}
