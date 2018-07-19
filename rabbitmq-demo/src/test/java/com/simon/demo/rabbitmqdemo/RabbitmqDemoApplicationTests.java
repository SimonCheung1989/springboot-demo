package com.simon.demo.rabbitmqdemo;

import com.simon.demo.rabbitmqdemo.service.Sender;
import net.bytebuddy.asm.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitmqDemoApplicationTests {
	@Autowired
	private Sender sender;

	@Test
	public void contextLoads() {
	}


	@Test
	public void testSend(){
		sender.sendMsg("Hello, RabbitMQ!");
	}
}
