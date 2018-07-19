package com.simon.demo.mqdemo.service;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    @JmsListener(destination = "queue1", containerFactory = "queueListenerFactory")
    public void processQueue1(String content){
        System.out.println(content);
    }

    @JmsListener(destination = "topic1", containerFactory = "topicListenerFactory")
    public void processTopic1(String content){
        System.out.println("processTopic1:" + content);
    }

    @JmsListener(destination = "topic1", containerFactory = "topicListenerFactory")
    public void processTopic2(String content){
        System.out.println("processTopic2:" + content);
    }


}
