package com.simon.demo.rabbitmqdemo.service;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Sender {
    private final AmqpAdmin amqpAdmin;
    private final AmqpTemplate amqpTemplate;

    @Autowired
    public Sender(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
    }

    public void sendMsg(String msg) {

        this.amqpTemplate.convertAndSend("key1", msg);
        this.amqpTemplate.convertAndSend("test.exchange.a", "a", msg);
        this.amqpTemplate.convertAndSend("test.exchange.a", "b", msg);
    }
}
