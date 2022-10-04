package com.next.mq;

import lombok.Data;

/**
 * @Title: MessageBody
 * @Description: 定义rabbitmq消息体
 * @author: tjx
 * @date :2022/10/4 20:48
 */
@Data
public class MessageBody {

    private Integer topic;//该消息体的具体业务表达字段

    private Integer delay;//延迟时间 毫秒

    private Long sendTime = System.currentTimeMillis(); //消息发送时间

    private String detail;//具体的消息内容
}
