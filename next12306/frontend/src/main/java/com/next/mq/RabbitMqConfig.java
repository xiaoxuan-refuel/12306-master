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
 * @Title: RabbitMqConfig
 * @Description:
 * @author: tjx
 * @date :2022/10/4 20:56
 */
@Configuration
public class RabbitMqConfig {

    @Bean(name = "directExchange")
    @Primary
    public DirectExchange directExchange(){
        return new DirectExchange(QueueConstants.COMMON_EXCHANGE,true,false);
    }

    @Bean(name = "notifyQueue")
    @Primary
    public Queue notifyQueue(){
        return new Queue(QueueConstants.COMMON_QUEUE);
    }

    @Bean(name = "directBinding")
    @Primary
    public Binding directBinding(@Qualifier("directExchange") DirectExchange directExchange,
                                 @Qualifier("notifyQueue") Queue notifyQueue){
        return BindingBuilder.bind(notifyQueue).to(directExchange).with(QueueConstants.COMMON_ROUTING);
    }
}
