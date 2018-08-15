package com.simon.demo.commondemo;

import com.simon.demo.commondemo.component.ServiceA;
import com.simon.demo.commondemo.dao.db1.UserDao;
import com.simon.demo.commondemo.dao.db2.BlogDao;
import com.simon.demo.commondemo.entities.db2.BlogEntity;
import com.simon.demo.commondemo.entities.db1.UserEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommonDemoApplicationTests {
	@Autowired
	ServiceA serviceA;

	@Autowired
	UserDao userDao;

	@Autowired
	BlogDao blogDao;

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

	@Test
	public void testBlog() {
		BlogEntity blogEntity = new BlogEntity();
		blogEntity.setTitle("This is title");
		blogDao.save(blogEntity);
	}

}
