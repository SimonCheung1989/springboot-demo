package com.simon.demo.rabbitmqdemo;

import com.simon.demo.rabbitmqdemo.service.MQOperation;
import com.simon.demo.rabbitmqdemo.service.Sender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitmqDemoApplicationTests {
	@Autowired
	private Sender sender;

	@Autowired
	private MQOperation mqOperation;

	@Test
	public void contextLoads() {
	}


	@Test
	public void testSend(){
//		sender.sendMsg("Hello, RabbitMQ!");
		sender.sendMsgByRabbitTemplate("Hello, RabbitTemplate!");
	}

	@Test
	public void testMQOperation(){
//		mqOperation.createQueue("test.queue.c");
//		mqOperation.createExchange("test.exchange.b", ExchangeTypes.DIRECT);
//		mqOperation.createBindings("test.queue.c", "test.exchange.b");
//		sender.sendMsg("test.exchange.b", "aa", "Hello test");

		mqOperation.handleMsg("test.queue.a");
	}
}
