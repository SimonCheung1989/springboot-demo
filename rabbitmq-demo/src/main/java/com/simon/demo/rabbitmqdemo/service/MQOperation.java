package com.simon.demo.rabbitmqdemo.service;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MQOperation {
    private final AmqpAdmin amqpAdmin;
    private final AmqpTemplate amqpTemplate;

    @Autowired
    public MQOperation(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
    }

    public void createQueue(String queueName) {

        Queue queue = new Queue(queueName);


        amqpAdmin.declareQueue(queue);

    }

    public void createExchange(String exchangeName, String exchangeTypes) {
        Exchange exchange = new CustomExchange(exchangeName, exchangeTypes);

        exchange.getArguments().put("arg1", "val1");
        amqpAdmin.declareExchange(exchange);
    }

    public void createBindings(String queueName, String exchangeName) {
        Map<String, Object> map = new HashMap<>();
        map.put("arg1", "val1");
        Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "aa", map);
        amqpAdmin.declareBinding(binding);

    }

    public void handleMsg(String queueName){
        Object object = amqpTemplate.receiveAndConvert(queueName);

        System.out.println(object);
    }
}
