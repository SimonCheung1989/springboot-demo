package com.simon.demo.mqdemo;

import com.simon.demo.mqdemo.service.Receiver;
import com.simon.demo.mqdemo.service.Sender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MqDemoApplicationTests {

	@Autowired
	private Sender sender;

	@Autowired
	private Receiver receiver;

	@Test
	public void contextLoads() {
	}

	@Test
	public void sendQueue(){
		this.sender.sendQueue("queue1", "This is queue");
	}

	@Test
	public void sendTopic(){
		this.sender.sendTopic("topic1", "This is topic");
	}

}
