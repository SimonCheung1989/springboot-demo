package com.simon.demo.commondemo;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.sound.midi.Soundbank;

import org.apache.fop.apps.Fop;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.bouncycastle.jce.provider.JDKDSASigner.ecDSA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.simon.demo.commondemo.akka.MasterActor;
import com.simon.demo.commondemo.akka.WorkerActor;
import com.simon.demo.commondemo.component.ServiceA;
import com.simon.demo.commondemo.dao.db1.DBHandler;
import com.simon.demo.commondemo.dao.db1.UserDao;
import com.simon.demo.commondemo.dao.db2.BlogDao;
import com.simon.demo.commondemo.entities.db1.UserEntity;
import com.simon.demo.commondemo.entities.db2.BlogEntity;
import com.simon.demo.commondemo.entities.db2.UserEntity2;
import com.simon.demo.commondemo.freemarker.PdfHelper;
import com.simon.demo.commondemo.freemarker.PdfUtils;
import com.simon.demo.commondemo.model.Notification;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommonDemoApplicationTests {
	@Autowired
	ServiceA serviceA;

	@Autowired
	UserDao userDao;
	
	@Autowired
	com.simon.demo.commondemo.dao.db2.UserDao2 userDao2;

	@Autowired
	BlogDao blogDao;

	Logger logger = LoggerFactory.getLogger(CommonDemoApplicationTests.class);

	@Test
	public void contextLoads() {
		System.out.println(serviceA.getAppName());
	}

	@Test
	public void testUserDao() throws Exception {

//		userEntity.setId(1);
		for (int i = 0; i < 100; i++) {
			UserEntity userEntity = new UserEntity();
			System.out.println(i);
			userEntity.setName("Simon" + i);
			userDao.saveAndFlush(userEntity);
			Thread.sleep(100);
		}

	}
	
	@Test
	public void testUserDao2() throws Exception {

		try {
	//		userEntity.setId(1);
			for (int i = 0; i < 100; i++) {
				UserEntity2 userEntity = new UserEntity2();
				System.out.println(i);
				userEntity.setName("Datasource2" + i);
				userDao2.save(userEntity);
				Thread.sleep(100);
			}
			userDao2.flush();
		} catch (Exception e) {
			e.printStackTrace();
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
		List<UserEntity> list = userDao.findByName("Simon", 0, 10);

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
		UserEntity userEntity = new UserEntity();
		userEntity.setName("Transactional4");
		BlogEntity blogEntity = new BlogEntity();
		blogEntity.setTitle("Transactional4");

		this.serviceA.insert(userEntity, blogEntity);
	}

	@Test
	public void testManualTransactional() {
		UserEntity userEntity = new UserEntity();
		userEntity.setName("Transactional6");
		BlogEntity blogEntity = new BlogEntity();
		blogEntity.setTitle("Transactional6");

		this.serviceA.insertWithManualTransactional(userEntity, blogEntity);
	}

	@Test
	public void testAsync() {
		for (int i = 0; i < 100; i++) {
			int number = i;
			Thread thread = new Thread(() -> {
				serviceA.saveAndSendNotification(number);
			});
			try {
				Thread.sleep(200);
			} catch (Exception e) {

			}
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

	@Test
	public void testTransferWithAkka() {
		ActorSystem actorSystem = ActorSystem.create("actorSystem");
		System.out.println(actorSystem.settings());
		ActorRef workerActor = actorSystem.actorOf(Props.create(WorkerActor.class), "WorkerActor");
		ActorRef masterActor = actorSystem.actorOf(Props.create(MasterActor.class, workerActor), "MasterActor");

		for (int i = 0; i < 100; i++) {
			Notification notification = new Notification();
			notification.setMailbox("simon" + i + "@mail.com");
			notification.setUserId("Simon" + i);
			masterActor.tell(notification, ActorRef.noSender());
		}
		System.out.println("---end---");
		System.out.println("Enter to exist");
		try {
			System.in.read();
		} catch (Exception e) {
		} finally {
			actorSystem.terminate();
		}

	}

	@Test
	public void testFormat() {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		System.out.println(format.format(new Date()));
	}

	@Test
	public void testCount() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(100);
		final int[] total = { 0 };
		for (int i = 0; i < 100; i++) {
			Thread thread = new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (Exception e) {

				}
				total[0]++;

				countDownLatch.countDown();
			});
			thread.start();

		}
		countDownLatch.await();
		System.out.println(total[0]);

	}

	@Autowired
	DBHandler dbHandler;

	@Test
	public void testDBHandler() {

		String sort = "+displayName";

		int sortValue = "+displayName".equalsIgnoreCase(sort) ? 1 : ("-displayName".equalsIgnoreCase(sort) ? -1 : 0);

		System.out.println("------asc-----");
		dbHandler.queryUserEntity("Simon", sortValue).stream().forEach(entity -> {
			System.out.println(entity.getName());
		});

		sort = "-displayName";
		sortValue = "+displayName".equalsIgnoreCase(sort) ? 1 : ("-displayName".equalsIgnoreCase(sort) ? -1 : 0);
		System.out.println("------desc-----");
		dbHandler.queryUserEntity("Simon", sortValue).stream().forEach(entity -> {
			System.out.println(entity.getName());
		});
	}

	@Test
	public void testList() {
		List<UserEntity> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			UserEntity entity = new UserEntity();
			entity.setId(1);
			list.add(entity);
		}

	}

	@Test
	public void testLogger() {
		logger.debug("This is log {}", "HHH");
	}

	@Test
	public void testWritePdf() throws Exception {
		String classPath = this.getClass().getResource("/").getPath();
		System.out.println(classPath);
		File file = new File(classPath + "/test.pdf");
		if (!file.exists()) {
			file.createNewFile();
		}
		System.out.println(file.exists());
		PDDocument document = PDDocument.load(file);
		PDPage page = new PDPage();
		document.addPage(page);
		PDFont font = PDType1Font.HELVETICA_BOLD;
		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		contentStream.beginText();
		contentStream.setFont(font, 20);
		contentStream.showText("You are overwriting an existing content, you should use the append mode. You are overwriting an existing content, you should use the append mode. You are overwriting an existing content, you should use the append mode. You are overwriting an existing content, you should use the append mode. You are overwriting an existing content, you should use the append mode.");
		contentStream.endText();
		contentStream.close();
		document.save(file);
		document.close();
	}
	
	@Test
	public void testAppendPdf() throws Exception {
		String classPath = this.getClass().getResource("/").getPath();
		System.out.println(classPath);
		File file = new File(classPath + "/test.pdf");
		if (!file.exists()) {
			file.createNewFile();
		}
		PDDocument document = PDDocument.load(file);
		PDPage page = document.getPage(0);
//		document.addPage(page);
		PDFont font = PDType1Font.HELVETICA_BOLD;
		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		contentStream.beginText();
		contentStream.setFont(font, 20);
		contentStream.showText("Hello Pdfbox...");
		contentStream.endText();
		PDImageXObject image = PDImageXObject.createFromFile(classPath + "/simon.jpg", document);
		contentStream.drawImage(image, 0, 50);
		contentStream.close();
		document.save(file);
		document.close();
	}

	@Test
	public void testReadPdf() throws Exception {
		String classPath = this.getClass().getResource("/").getPath();
		File file = new File(classPath + "/test.pdf");
		PDDocument document = PDDocument.load(file);
		PDFTextStripper textStripper = new PDFTextStripper();
		System.out.println(textStripper.getText(document));
	}
	
	@Test
	public void testFop() throws Exception {
	}
	
	@Test
	public void testFreemarker() throws Exception {
		try {
	        Map<Object, Object> o=new HashMap<Object, Object>();
	        //存入一个集合
	        List<String> list = new ArrayList<String>();
	        list.add("小明");
	        list.add("张三");
	        list.add("李四");
	        o.put("name", "http://www.xdemo.org/");
	        o.put("nameList", list);
	        String path=PdfHelper.getPath();
	        PdfUtils.generateToFile(path, "/pdf/tpl.ftl", path + "/pdf/", o, path + "/xdemo.pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMultipleDB() {
		try {
			UserEntity2 userEntity2 = userDao2.getOne(1);
			System.out.println(userEntity2.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
