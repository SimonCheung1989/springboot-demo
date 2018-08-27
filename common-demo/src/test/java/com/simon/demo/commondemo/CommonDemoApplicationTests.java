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

	@Test
	public void testCoundByName() {
		long total = userDao.countByName("Simon");
		System.out.println(total);
	}

	@Test
	public void testTransactional() {
		UserEntity userEntity =  new UserEntity();
		userEntity.setName("Transactional4");
		BlogEntity blogEntity = new BlogEntity();
		blogEntity.setTitle("Transactional4");

		this.serviceA.insert(userEntity, blogEntity);
	}

	@Test
	public void testManualTransactional() {
		UserEntity userEntity =  new UserEntity();
		userEntity.setName("Transactional6");
		BlogEntity blogEntity = new BlogEntity();
		blogEntity.setTitle("Transactional6");

		this.serviceA.insertWithManualTransactional(userEntity, blogEntity);
	}

	@Test
	public void testTransfer(){
		UserEntity user1 =  this.userDao.findById(1).get();
		UserEntity user2 =  this.userDao.findById(2).get();
		System.out.println(user1.getName());
		System.out.println(user2.getName());

		for(int i=0; i<10000; i++) {
			Thread thread = new Thread(() -> {
				serviceA.transfer(user1, user2);
				serviceA.transfer(user2, user1);
				System.out.println("User1.score=" + user1.getScore() +", Total=" + (user1.getScore() + user2.getScore()));
			});
			thread.start();
		}

//		for(int i=0; i<10000; i++) {
//			Thread thread = new Thread(() -> {
//				serviceA.transfer(user2, user1);
//				System.out.println("User1.score=" + user1.getScore() +", Total=" + (user1.getScore() + user2.getScore()));
//			});
//			thread.start();
//		}


	}


}
