package com.simon.demo.webdemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebDemoApplicationTests {

	private Logger logger = LoggerFactory.getLogger(WebDemoApplication.class);

	@Test
	public void contextLoads() {
	}

	@Test
	public void testLogger(){
		logger.debug("This is log {}", "HHH");
		logger.info("This is log {}", "HHH");
		logger.error("This is log {}", "HHH");
	}

	@Test
	public void testException() {
		try {
			String str = "";
		} catch (Exception e) {
			System.out.println(str);
		}
	}
}
