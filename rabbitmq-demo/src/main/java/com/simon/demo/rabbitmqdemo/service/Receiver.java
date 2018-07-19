package com.simon.demo.rabbitmqdemo.service;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    @RabbitListener(
            queues = {"test.queue.b"}
    )
    public void processMsg(String content){
        System.out.println("******************");
        System.out.println("processMsg: " + content);
    }
}
