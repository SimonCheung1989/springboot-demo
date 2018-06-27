package com.simon.demo.profileservice;

import com.simon.demo.profileservice.kafka.Consumer;
import com.simon.demo.profileservice.kafka.Producer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaTest {

    @Autowired
    private Producer producer;

    @Autowired
    private Consumer consumer;

    @Test
    public void testProducer(){
        producer.send("test", "helllo11111");
    }

}
