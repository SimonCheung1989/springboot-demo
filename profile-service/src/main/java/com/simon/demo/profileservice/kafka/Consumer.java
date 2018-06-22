package com.simon.demo.profileservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    @KafkaListener(topics = "${kafka.topic.testtopic}")
    public void receive(String payload) {
        System.out.println(payload);
    }
}
