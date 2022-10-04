package com.next.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @Title: RabbitDelayMqConfig
 * @Description:
 * @author: tjx
 * @date :2022/10/4 21:05
 */
@Configuration
public class RabbitDelayMqConfig {

    @Bean(name = "delayDirectExchange")
    public DirectExchange directExchange(){
        DirectExchange directExchange = new DirectExchange(QueueConstants.DELAY_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean(name = "delayNotifyQueue")
    public Queue notifyQueue(){
        return new Queue(QueueConstants.DELAY_QUEUE);
    }

    @Bean(name = "delayDirectBinding")
    public Binding directBinding(@Qualifier("delayDirectExchange") DirectExchange delayDirectExchange,
                                 @Qualifier("delayNotifyQueue") Queue delayNotifyQueue){
        return BindingBuilder.bind(delayNotifyQueue).to(delayDirectExchange).with(QueueConstants.DELAY_ROUTING);
    }
}
