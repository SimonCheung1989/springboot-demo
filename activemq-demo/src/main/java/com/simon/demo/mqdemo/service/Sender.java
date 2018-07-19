package com.simon.demo.mqdemo.service;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Destination;

@Component
public class Sender {


    private final JmsTemplate jmsTemplate;

    @Autowired
    public Sender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendQueue(String destinationName, String msg) {
        jmsTemplate.convertAndSend(destinationName, msg);
    }

    public void sendTopic(String destinationName, String msg) {
        Destination destination = new ActiveMQTopic(destinationName);
        jmsTemplate.convertAndSend(destination, msg);
    }
}
