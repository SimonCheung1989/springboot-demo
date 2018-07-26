package com.simon.demo.rabbitmqdemo.service;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Sender implements RabbitTemplate.ConfirmCallback {
    private final AmqpAdmin amqpAdmin;
    private final AmqpTemplate amqpTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public Sender(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate, RabbitTemplate rabbitTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMsg(String msg) {

        this.amqpTemplate.convertAndSend("key1", msg);
        this.amqpTemplate.convertAndSend("test.exchange.a", "a", msg);
        this.amqpTemplate.convertAndSend("test.exchange.a", "b", msg);
    }

    public void sendMsg(String exchangeName, String routingKey, String msg) {
        this.amqpTemplate.convertAndSend(exchangeName, routingKey, msg);
    }

    public void sendMsgByRabbitTemplate(String msg){
        this.rabbitTemplate.setConfirmCallback(this);
        this.rabbitTemplate.convertAndSend("test.exchange.a", "a", msg);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println("----------------");
        System.out.println(correlationData);
        System.out.println(ack);
        System.out.println(cause);
    }
}
