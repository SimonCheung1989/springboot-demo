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

import java.util.List;

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
	public void testUserDao() throws Exception{

//		userEntity.setId(1);
		for(int i=0; i<100; i++) {
			UserEntity userEntity =  new UserEntity();
			System.out.println(i);
			userEntity.setName("Simon" + i);
			userDao.saveAndFlush(userEntity);
			Thread.sleep(1000);
		}

	}

	@Test
	public void testBlog() {
		BlogEntity blogEntity = new BlogEntity();
		blogEntity.setTitle("This is title");
		blogDao.save(blogEntity);
	}

	@Test
	public void testFindByName() {
		List<UserEntity> list =  userDao.findByName("Simon", 0, 10);

		list.stream().forEach(entity -> {
			System.out.println(entity.getId());
		});
	}

}
