package com.next.controller;

import com.next.mq.MessageBody;
import com.next.mq.QueueTopic;
import com.next.mq.RabbitMqClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Title: MqTestController
 * @Description:
 * @author: tjx
 * @date :2022/10/4 21:26
 */
@Controller
@RequestMapping("/mq")
public class MqTestController {
    @Autowired
    private RabbitMqClient rabbitMqClient;

    @RequestMapping("/send.json")
    @ResponseBody
    public String send(@RequestParam("message") String message){
        MessageBody messageBody = new MessageBody();
        messageBody.setDetail(message);
        messageBody.setTopic(QueueTopic.TEST);
//        rabbitMqClient.send(messageBody);
        rabbitMqClient.sendDelay(messageBody,5 * 1000);
        return "success";
    }
}
