package com.simon.demo.rabbitmqdemo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfiguration {

    @Bean
    public Queue key1Queue() {
        return new Queue("key1");
    }

    @Bean
    public Queue key2Queue() {
        return new Queue("key2");
    }
}
