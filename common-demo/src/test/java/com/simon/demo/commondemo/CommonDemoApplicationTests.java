package com.simon.demo.commondemo;

import com.simon.demo.commondemo.component.ServiceA;
import com.simon.demo.commondemo.dao.UserDao;
import com.simon.demo.commondemo.entities.UserEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommonDemoApplicationTests {
	@Autowired
	ServiceA serviceA;

	@Autowired
	UserDao userDao;

	@Test
	public void contextLoads() {
		System.out.println(serviceA.getAppName());
	}

	@Test
	public void testUserDao(){
		UserEntity userEntity =  new UserEntity();
//		userEntity.setId(1);
		userEntity.setName("Simon");
		userDao.save(userEntity);
	}

}
