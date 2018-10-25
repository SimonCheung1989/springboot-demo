package com.simon.demo.commondemo;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.pdf.BaseFont;
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
import com.simon.demo.commondemo.utils.FileTools;
import com.simon.demo.commondemo.utils.MyHelper;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import freemarker.template.Configuration;
import freemarker.template.Template;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
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
		for (int i = 0; i < 10; i++) {
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
			for (int i = 0; i < 10; i++) {
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

	@Test(expected = NullPointerException.class)
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
		for (int i = 0; i < 10; i++) {
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

		for (int i = 0; i < 10; i++) {
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
		CountDownLatch countDownLatch = new CountDownLatch(10);
		final int[] total = { 0 };
		for (int i = 0; i < 10; i++) {
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
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAppendPdf() {
		try {
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
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Test
	public void testReadPdf() {
		try {
		String classPath = this.getClass().getResource("/").getPath();
		File file = new File(classPath + "/test.pdf");
		PDDocument document = PDDocument.load(file);
		PDFTextStripper textStripper = new PDFTextStripper();
		System.out.println(textStripper.getText(document));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		int i = 1;
		while (i<10) {
			try {
				UserEntity2 userEntity2 = userDao2.findOne(1);
				System.out.println("Time: " +(i++) + ", " + userEntity2.getName());
				Thread.sleep(	0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Autowired
	@Qualifier("dataSource1") 
	private DataSource dataSource1;
	
	@Autowired
	@Qualifier("dataSource2") 
	private DataSource dataSource2;
	
	
	@Test
	public void testDBPool() {
		try {
			int i = 0;
			while (i<10) {
				Connection connection = dataSource2.getConnection();
				System.out.println("Connection: " + (i++) + ", " + connection.isClosed());
				System.out.println(dataSource1);
				System.out.println(dataSource2);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testFreemarker2() throws Exception {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
		String classPath = this.getClass().getResource("/").getPath();
		System.out.println(classPath);
		cfg.setDirectoryForTemplateLoading(new File(classPath + "/templates"));
		
		Map<String, Object> root = new HashMap<>();
		root.put("name", "Simon");
		root.put("sayHello", false);
		root.put("id", 1234);
		List<Map<String, Object>> list = new ArrayList<>();
		for(int i=0; i<10; i++) {
			Map map = new HashMap<>();
			map.put("name", "Simon:" + i);
			list.add(map);
		}
		root.put("list", list);
		
		Template temp = cfg.getTemplate("test.ftl");
		
		Writer out = new OutputStreamWriter(System.out);
		temp.process(root, out);

	}
	
	private String generateHtml(String templateName, Map root) {
		String result = "";
		try {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
			String classPath = this.getClass().getResource("/").getPath();
			cfg.setDirectoryForTemplateLoading(new File(classPath + "/templates"));
			Template temp = cfg.getTemplate(templateName);
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
			temp.process(root, bufferedWriter);
			bufferedWriter.flush();
			bufferedWriter.close();
			result = stringWriter.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Test
	public void testPdf() throws Exception {
		try {
			String classPath = this.getClass().getResource("/").getPath();
			Map root = new HashMap<>();
			root.put("title", "PDF v1");
			root.put("business", "中文");
			root.put("signUpTime", new Date().toString());
			root.put("accountNumber", "54645355345464");
			ITextRenderer renderer = new ITextRenderer();
			System.out.println(this.generateHtml("pdf.ftl", root));
			renderer.setDocumentFromString(this.generateHtml("pdf.ftl", root));
			ITextFontResolver fontResolver = renderer.getFontResolver();
//			fontResolver.addFont(classPath + "/font/simsun.ttc", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
			fontResolver.addFont(classPath + "/font/arialuni.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
//			fontResolver.addFont(classPath + "/font/Arial.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
			
			renderer.layout();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			renderer.createPDF(byteArrayOutputStream);
			renderer.finishPDF();
			byteArrayOutputStream.flush();
			ByteArrayInputStream byteArrayInputStream = FileTools.byteArrayOutputStreamToByteArrayInputStream(byteArrayOutputStream);
			System.out.println("------");
			System.out.println(FileTools.fileToBase64(byteArrayInputStream));
			byteArrayOutputStream.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPdf2Base64() {
		String classPath = this.getClass().getResource("/").getPath();
		System.out.println(FileTools.fileToBase64(new File(classPath + "pdf/pdf.pdf")));
	}
	
	@Test
	public void testPdf2() throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		String classPath = this.getClass().getResource("/").getPath();
		System.out.println(classPath);
		
		root.put("staticPath", classPath + "/static");
		root.put("uniqueNumber","SullanFang");
		root.put("businessName","简体繁體businessName");
		root.put("signUpTime", new Date().toString());
		root.put("accountNumber", "54645355345464");
		root.put("business", "Sample Business");
		root.put("publicImageLink", "https://sacctcihkpeakng.blob.core.windows.net/public-web-content/public-web-content/onboarding/images");
		root.put("termAndConditionLink", "https://payme.hsbc.com.hk/");
		root.put("contactUsLink", "https://payme.hsbc.com.hk/");
		
		ITextRenderer renderer = new ITextRenderer();
		System.out.println(this.generateHtml("notification.ftl", root));
		
		File htmlFile = new File(classPath + "html/Notification.html");
		if(!htmlFile.exists()) {
			htmlFile.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(htmlFile);
		fileWriter.write(this.generateHtml("notification.ftl", root));
		fileWriter.flush();
		fileWriter.close();
		
		renderer.setDocumentFromString(this.generateHtml("notification.ftl", root));
		ITextFontResolver fontResolver = renderer.getFontResolver();
		fontResolver.addFont(classPath + "/font/arialuni.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
		fontResolver.addFont(classPath + "/font/simsun.ttc", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
		
		renderer.layout();
		FileOutputStream fileOutputStream = new FileOutputStream(new File(classPath + "/pdf/Notification.pdf"));
		System.out.println(classPath + "/pdf/Notification.pdf");
		renderer.createPDF(fileOutputStream);
		renderer.finishPDF();
		
		fileOutputStream.flush();
		fileOutputStream.close();
	}
	
	@Test
	public void testMyHelper() {
		System.out.println(MyHelper.NotificationType.CASHOUT);
		
		MyHelper.NotificationType type = MyHelper.NotificationType.CASHOUT;
		
		switch (type) {
		case CASHOUT:
			System.out.println("Cashout");
			break;
		default:
			System.out.println("This is default value");
			break;
		}
		
	}
	
//	@Test
//	public void testBase642File() {
//		String classPath = this.getClass().getResource("/").getPath();
//		String base64 = "JVBERi0xLjQKJeLjz9MKMyAwIG9iago8PC9GaWx0ZXIvRmxhdGVEZWNvZGUvTGVuZ3RoIDY4Mj4+c3RyZWFtCnicnZbLTtwwFIYtzc5bbgN0JFcqvQjk+pI4jtQV4jKlVHTagRlRugINFSqVYNOX6zv1FfofJycwsyAoijz2H/v854tjO3Mnd8fSqJhZHaMaX8n9sRzJO2m08UWu/kinjtB/I61Rn+X3H0ZdSR9UNDmNv5V5XmoXWf9i7YM2GSSGPmo2o37KifxNOUIIhVqs769lcNpG5VyufYksLDOyClYbEk6XmUK6wrG4lHnUyMN9hXaBo1gkRwysZdB4So4qdMwbxyQuJWdLkjlS1BzjJZ5pxpzG65A9YBunXcnYxmpbcpJKNNh1X4VWRbFIjg22aZhN0LZxo3ZDnBQTNLRs9EBrI179wyTbaHTOk2yLUmc8ybVgWu5LRHUUi+TItDZ67RjYxkx7nuRaMHItmSNFzTFW2EbRhUWClfv+wCrr1HgmbbprVeZUEQO9/vGtfCteiZ7YQnmN8ubd+IbWdx3nMopD8D1WYhmw1h97ZLYy2BZDcYLrq+i9/LdzMRO9nd1UiWWyMxRuEftM31DiHfs265VO1tjDmLEW69Uu1jmofWyzXutkDepW6H4X5wzQoXWq1ztZA7popd7oYu3L6iR+2nqzkzVKi++LLr6udM9Y08tiadH8iQ8A721Di2Nud7vca19vzg/IMxWncD8Tx+JIjNCaim/4PQTHROzh7gn6euIcrYn4lPpJ7yFqmGpSH+FTUZ9DD+bPiUUGOmotzq+yYthKjsfIPURNbiO4fknulOMIfWfIPUzqcI7FY+TFhTBpMEGcpuNq3nAgeupvXG2FsmWkD1197B3goskZIRUhTYG0nyAGtKHEmlhH6YtVFPpd43vteYpAX6aUJyxM+wSaHp1yFHjEKeoATX0rYiMtgz56+1ADtJdxZ1tojKX+JejNOj/9CRnJ//a1jjwKZW5kc3RyZWFtCmVuZG9iagoxIDAgb2JqCjw8L0NvbnRlbnRzIDMgMCBSL1R5cGUvUGFnZS9SZXNvdXJjZXM8PC9Qcm9jU2V0IFsvUERGIC9UZXh0IC9JbWFnZUIgL0ltYWdlQyAvSW1hZ2VJXS9Gb250PDwvRjEgMiAwIFI+Pj4+L1BhcmVudCA0IDAgUi9NZWRpYUJveFswIDAgNTk1LjI4IDg0MS44OF0+PgplbmRvYmoKNSAwIG9iago8PC9MZW5ndGgxIDM4MjkyNC9GaWx0ZXIvRmxhdGVEZWNvZGUvTGVuZ3RoIDcwNjcwPj5zdHJlYW0KeJy0vQd8VFXeP/w757bpPZmZFJJhCAkECDCkYTAjNQKyLFIiEopSYqhZREBAQDYGiIiIFJFFFhFZFjUgQkTsKFgillVwbYuKZR/wcRXBVTLzfs+9kxCQZ//P/30/74zf+Z177qm/fu4MkRgRmWkJSeS6+bZbM3sNS+mPms1EtnmTZ02Z/tceM7YT2X1EWtWUafMnLz2fPJXIt4Qocrpy0oSJxyqvWUhUNg19CipR4bmn+glcow+1q5x+67x5y/b/HddHiY/i02bePKH9fau3En/oTZK8OdMnzJv1/r3ZXUnZexbtM2f9YdKsb7/YNI6UJzFfu6dIfykHKYjPHeKz9UvOMmriX19aH6sUNeLzl/4UlL8mO38x/iNGsdP/9cuUwP/n1woqo5T43HhD/CvaTuPJHh8T3xr/kR3hxa2bybPl2TQmvp1eo+fpJWqgfbQLnwRK9Bda36p8NxGfi7tb6XFcr6Ud+r21wOP0iDEaG8uq2BY2k41k11y2niVAI96VNJiFrrDenXhvpfko3U1LaSHeb7IgjcN7NT3PZ9ECyYS5GhKtR8X/otPpNAwQr5FAdXw1WjTSm3gT3YnVz2HaJbPU0liqwUyraXJLXQfazR/jt/MZ7B4ay5fQZvYsvcl30y98F83gv6MHjWbKdAryGjJBvvtoDd1BqzDzRsqKf08Hcd2NXqZ0irJ1uLsT84ykgXppi1FmH0HDLZRMKTQxvpm6x9+iIfr7frz3goOC99vwXkyLpa28XFrM+ze9L2VBPiPjJnkzcbzXxzrTLXQrjZSnk0X1qdvj52LjpeksF7IwdHcH1nSGZmP/D9AmmkX36FeNLXvthdpN+JxM02ig5KWd7EO9/kHarUt5BE3Ur2fivQJSfVreKu9tVT+BFuHzODCupeSnEHWmYhpKN9ECupe2XybZntSfRoPjD19B6g/Sfkh9P7RqG3i1Hu8rv07Sp3S3NJmGShcon12FteXwXWwpuDFC6kuz2A4aRPPE+th8OsuSKZfeazXHGqx1fvxk/Dv+HHnwfguaNINeAVq/NmP1a+k+fS8zIb3u2PWVXjfhXUZlzIF3B9YBnOkubZfW4r1dmUM3sQz6VDosB7HnceIe9tZcIvYhOybtlf7C3mZ/Z19SPuVBb3ry5/ir/BBk9Qv2MJgfhXSW0ix1r7qX3avMU4WNTcb9OXQj3Y6Rtstj6Wk+lpaxAD3OxiVW1VKSG2iQNIsdlD6RG/kUZlhMdyoBhyZCF4TUtv4PdZ8qz1N/OY2+pdukN8CB1yDT21iBvvoZerta8PJBeuRKdQm6FaUn6GlYQyPNvULdbdSPfmJmVthCG6GPPfG+gH3n4/3/1+uP8C4j6Xc05op1/Wlaom4W3Qy9ar2zJ65Y1x/cGQ0uNNMR4MnlbcSef8sDumLdlfpeqe5B6Um5Tq6TnoRWdKSjVA1tGAb+rcJ7LztMUSqSR8oj/9d8uR3vmVQOaQ7CTsZjvjvBj4GoufRVcYXVXF4jXhPRt4qGsknQ7RkkvLaIaH+ie+RPycOepCxazpJoOTHawD6jerTpreXQ54irn9IA1N/BLHinQLunwFs3oecNsMhtsKM5kNQqrPAO2kDLaBRkch/QBTo1mFz0EEbaASu6CjMep+O8P8pXeKn71fvJp3qUm8glPySvlaow86/xf8X/q+m7SxoK+2iWrdD0heDL3fBQW7GSffDldraG7aLnftNu3iXt/slGYFU7MV7DlVbz//JVEH8dOx8d30Ex5VoKQwKFmHkHTWT9YFdbm96n7mw15h7PX7ywJvYheEaUitVNRQSq4GaJ5Bel01jdVn2lH7ODtJIcpFCpZJM+hX97mqqUq9ga+kA9yKag3xBqyzZLGnhgofPgbzn1ke0o/0hz+WOk8ACbC/1YQnV0UtpKyWwU4s9b/DpptnSndPLisqEHe+DDR2IVw+hZ2PtrdICGSU0Ybzs4WK9sEa3i/0SWUAu534hakXEsZAp2dDfvyvvCaz9HA3kZnweNGM7LoQdvCd2CFfzViDyaIzHTJvBlATzpOkSD+ygGGW1gU+RPsGpibvjaBZjpHJrOhfXFML7xGgtOLNWjUR1lg1bBL5Si/0zEhy/xNjKV60XvS19KfmLe9VjxQHiVZcAtKI2Ff+sk7Qd3iY1jUcQuirdoWmLeP7NHuJ9ms530LjR8HmRJqoxIJMbzwRrbIfMcgZX5MN5mrKZKfhvyFK8K6kFH46dQegYcvSf2J722F2z5Nt6PtrFO7BB4mU5fwTMo8d7xXzHqJsTLZOx7E3Y2EpozCNKYhLHbUxFq//hbdZNd1FFfSzn4NhCa/Ddo+3aUxyIOBqXXKIC9PSyV8Nuxt2/QYSjuTEns7UHpE8S6RujQUuzhTvSezRqk3eyIlkYvsieumBf8X77URhFdTSLLzIdsc7CvWcyBbKU/Pciukj8mkXHeBg/RM8H7vq14fz/WtB1c340Ww3ga7YGM5oGDG8G3e+hR2Mx1opk2JCHfWdj7ZPjhBei5Ui8/yzsqhwQP6FHmh49K8EDqztsk5ijC2PczFbnBH7Gq2fCO90k/YZbhtIQjf41Gh11b1uuqnsVFhQX5PSLdu3XN69K5U27HDjnZ7bPahduGMjPapKelpgQD/uQkn9fjdjkddpvVYjZpqiJLnFEnFqgP9CnvV1Uf7DO+vn+4b9iVWd9/yPfX5dWTJzUUdmdG8m7onGhVr+TWk3dQvW9o+R6KFt1Qr+Ze3mRIvZTl+iGEztelZvarl7PwX3jghIn1OcPKQ2HXB6kt929An/qUPuWhUGo9z8J/1+IW/hs4IXNivWso6kOpRs219TS0XKAh/nkRKqkodAM+h5XXt2m+vOGGKy3yaXD0hZZlRtBkCFvp2tM/2KdvPfn2UP/P6ylJNPq+CPGkpD4nF8twoaSPRXn1zPdDPfPWs6TrsOBLJxDd/lF0BQ70m1gV7jfxFvBz4viLHP3e4Gcoc2XmymHl7giK+pIH1R/9ffkeq6VPuM8kCypIr6A9FitqrKICQ8zaw/pfzfQC79+v5x5OJjuY5xHL7SdQVR+tG49CuC+4hjvei3ca4i/c3foWoVtzyWuUjEXUq33qNWMRmbfURyfUU13mnk4vrLy7wUU3jc+1TQxPnDCmvF6agAZ7SMrqVzm8Pm3Q0NGowlTA+MpMIey++ocQXWa/ysyVuBZtx+Mz3FeI/JL6iZWTxgslYePDfXHP3Ke8NvRCar0HtF+9O7d+AJoNuP3LVGllv8AtmeJy5crazPqtWG6ruyHxCRUIYOkr+4UxGwbrV9VbiCSvRWy6Ll47URdOtG5CZv2Sm6oMzZtwd7P2h1a66vufC0E6kA966h0TrJw4vkosuWqC2Ga/qsyVdZP0rd6tbw3amtmvqq+A6AjdpxHoPbq8X2W438UJsXEUpKzL+4ZC9cFc0XHlyn5iiRMmYvXGknHj4vqFRaTmMqynT310uE5ouC4DzBid0PeGRFWiwWjRTdwZ3/eGG0KG3NG0XsuqVbqEM1eKEbWsel+uK3QY917o3GnQsPJ+fVP13dfzPuW9zgRSz6A8aGhLNQugzcq8M6kGjwZdHx70e0MLKps/xg83zJe3SB5NE+31URsDqY0o9w/3H79yZf9wZv+V41dOaIgvuSmc6Qqv3DNo0MpZ/cZn6nbPUH+wLrW+/9031LvGV7KeELLQt/7DhGT6Z1ZOMLxEaThUlBpy39B8e+j/dDthYlB2qLwwsZWu01iWDa4oNbO/8CsNcAip9a4iYaFYxIhymMDNurrqHzCN6zF4qjAS6Yasfrdcn+ANFDGhK8Lh/T5Ri0FCIWE+dQ1RugkX9Ut+X25cZ9JNqXspmpcLsY0Xd15ovpM0QtxZ0nynpfv4MMQUGHT9/0GdW6vySnfYk1mcp7Ne97MT618Yjj3+XFRvKkpI2tunXErliRJPlUTJkgvPVVLvz9U7Cp7AQa50hTPfDte7cuuVPuUvpJbckOlyw7OxFj1IjCg01PV2+DUm/Cf5XPWspJ4li3qCP9WduuQvws2Wjpn9Vo5PaJjYHmSnM7L+FJzJnlPU94bQZTtPBIeJlb/dvg3bRxtXuN52LtVo7/aEBRPe1E3gcsW4fPWDhreUhpUvSr39BmRUxMUDSI1wrpZAUqNWjcmMFMksk8n1aSP+o7xIY15jt64hd8idhQ+Gxr8sUehXQQkF4jQm/rXsV14kM/npQPTOnmwg44OdrF/ypGTex3Ozh5dYB1u5NEBiFJGYVIZZe3LGy0ygJua1hW0RWx+b7E0OJ0eS+yTLac5OzhLnYKec5u3kLfEO9so7VbZRYRuISZqFFblZoYWZ3MzK3Q+QZnlA8dpXcN8KLRrkC3lAW8Bzc1lKwHXdGdcZKj1Tembc2Aq8xlVUdOvKKqjCuGh+s3F6Bct0u3gYHx5vd4+7RxcWbsvdPk8yL2afnv86Fj7/1Zr7r1l285r7et+pvNjkidXEVvPTbAGb/ctBFmSln8YXxT6PHf4orj9AwHnuC8FS8MREg6PFXH6wXKlU5inLFVnRVKZRXaU8T14uN8iyPICNYlPYBvYoUzTmZ9lsAJvLatkBtGJ54yqq36poghRKP6hoegViCLtVLb9dYUSifbWfs8LqOfKCJTc8vu5jMWcpslML5kyhc9GHtCCzk8nJeECRlFXJAV9ycmBnMstOZkxz+93cxS0um7NgijRX4kqyFJCDbrvd8oBbCz4YZR5TkNn4o8mMnkpmc5M3JD+aLHEtuTD5fLLkMEknJJ7OJFdysMAisV8g0nJpo8S5SWJWKVnBWIy0FT4t6A8WBs8HZUfQxFg6MfsKRzTNt5CnOi6K6G8QzitjK1xnDnv8xSgLkVTnMpREuWJcNUoUKE25DnKM5LnRqCKX9HYVtY4ugetyax2LDkOu6De2ogPrUVB4Ncvv0T7cVtWyC9pFuiNH5IqqhWTLhfzHzqz58k/z5m9nT7uZdOTEv96IvfNQD/7Owtjzt8aXzlyz7v6kt/7+339ZFfvsh8VjBD9HQ69Twc+OLBB12zKZQyoMPhrkHKx1siC8R7SbzVGQ7Sx0cp7jLnJzKGXAXemWRnVgfGCYSaOymKlNoE1RG8mm2f127ud20SsPfC9Ty1XOk8j7wPFklpysZmkhZguF2jyQpfktjCwW+wNR1avlMMhJ9ClEHy3kDxWGMJbEuAZb4y5GOUlJbVNX5ERzog5PQY5rhRSqa2upM0c7SQt5rvkinz+AMRxOlD3Fee6IzsOxFTqrDQZHoG2CyWMr3GhR0VT86jiD12N1yxGSwRlLkIQE8MHasCQfhdu2zy5swyLdC3TWa4Xdr+bNQria6ULQJNPyutgnb7x0IWlf6uo5dzzx0JIeg5LKhlf2XTNu+grPvvB/7zjw02tSMOW9hUdj8c8PBtfcu2fJgm3eLY6CSYOmz1u5JPT8c59sbRwt/E0G9NynHMTp1UH3Rsc/6mA8y/6Ina9zMJvDcY+q+VRVW6s+rD6pSuCWanZsZrIsaarkIZPG62xTJGaScqQiqUySG6RvpJ8l4ZwK8cElyVGklqm8XK1U56nLcfJUwZaKSCSvohrvkqbikleptBQMKmkqKXEXj6tA1pErL3IJJRR+hYXdYXcon0XckZA7zGTfq/uaaviD978au0uOFbOxsW1s7A5p/4WBfH1TleErRkLPQsoOSmOWaPpxNxvhnuTmvNDORtmn2PkodQqWI1fKnKU1xN+Oerv3KBjtq/LxIhebZ2FHZMZJ1I9H/dfE+mFAYv40vANpq4IBXzAY6OdkEvHACf83/p/9kj9p8wG4X25KezCoFtkY2WzOB0zen4KMHw9+HYQWBwNBbs4OFAYGBEYFZJMn4OE2noq0OxpMTi9I9axQ/BRMkwO2FdZoG2UhT7deVLRX/d1dZxYdzoVyVBvmDO1K6JauVq9WdPck2GYC2zST4Fx1Ra7uh6FZCV/NCiLdSdetdpfoVrY3lBRife+69wBzxU7/40DsrO+Qf/2MFbseX/FYxa7V/HhTg1Q9JtZ04u+xY8fecNStOLZh095VPv6D+KaG67xOh037KczKoo9PSmF8WhosNQnMZlPCjKYFWZWLTfMyaUoSo5vtiFdexqZpLNK2T9vr20olyYOTuXQ080Qmnwff6Kx18p22BhuH1RZZGO/Ylr3e9u9tuZSdxLg/ibFyL9vgYvyoBbELnl9m7EgA0jC12azBH4ziktmPeAhpODwe3wMOLfBA1OQ1pQZSc1Khu/DU2W7JyeEdelAIEngyN1IgaLRdVnYB8lHy1LlTVygaTcERfQPJVk4QShaE0q6VUMYJD/u3M8Kuc3WL1626tV8tzhsHcZ0RktFtXZdcIkRWQyjVzWIRLtm4qYP0GApZeeBow23J7SLYu19rL+InSwhNTfIlM9vCU/su/Pr2e7HjzB5/5Mvgvm5HVr7K+i/ctOnOubv/ws++FDt37J3YR6yIrWbLWV3DU65vY9/FPmyqPXrPPQefWH3fW7qt3ATb98NWLFQfnX29yqRCBlVWFXUV4z4UFa4yWUP68qBZlT0BrUhbDnd5QGF8gDJF2aBIUhHKSCakTWhtYgGWgxnLWDlTzQjDtRiMYThNIWlF1CQHZO4gGcPZaCG3mnSOglmBPDAQvBMxqyIRtZrf8A5nikWgQny6GJ3AJ6aSFPaQN5Qfkv0nYofTGjQW/EdTRHpKST3T1D92N8vlbzAu9FTS9VTEnmRqS11Yt2idJTUltSp1U6ps25izM4cL7ay0M1OHQIeyDhLfgGijhf3hAWEpkF6WPi9d8vsH+Of6JT5XqVW4iQLEHZLVhHhF1g4PaH6/uOkkv19JR8wPPxBVvOUpbFQmG0CszMqyO7Gc9kzz+X1QzICDC13lJlPAhLyhk4hHZsSj9iKByBRXbVFIEVealTk5WR0pK3xRX9TtK/C1X8EzEfcdQmddHl+Bo9MKU7Qrf5/nmf7Wop+HWxK2izFKZAO5uYa6tryaVVboK9S1uFgPRQlt1FVXFAiBSXb7ZOhjPjKC/Pb4NKJQdiuN9LYqS8hDYz/87YPYjxuWfPzNoef/6/ixSSvqJk25a+WkxbueuGPZ9r9IwfLYyWdjjF6seztZ7ntq24f/eOjEdX2WTrh5+V03Tl/clLp92bKdj92+eDfpfqY8Ib8AtWO+6LNT/KwynVVaDY8yRXc1lSFWiQxrdDobY2XX+9kIO4vY+9ivt0tSIYQ3yi/RHD+T5rmWuzivtTM2AP7l/fRT6VwKJOckczbCN8nHpXnu5e6NbqkQUdCkMZPCWFFGWUZ5xjcZMidzcHOZCHYBpGiSmSTJmfGAWfN6kx5werW2/rbZbQ+0lW05VEQ8La2tiZgb/sPmrfO0XaFG29sW8iy1Va4G+YDbbnhxOA/hN5oFUX2J42g2hNZWoefY4/Ts27hkDpFi5/eAuxBOQ08hkuFG3EprsUzMf3raU02Mv/Em6xj7vumRTwO7O7y+oz7W+McHH1q4CG5j78gqZv7g7yw59lJsfmx2bN5TDY6vWR4zhZ9ZveblY2vr9hjyaIDzqJGz9HNOZtSNs5xTrVc8JD/BBig48sD9IaDrxwVsI5wfcQOsphEvqbzx148aicdjREo58g6NnCw52qanmcGjWxQHTM/KNBtSXBNnzOyUZNlhxck/Oiont2CSlTG/9YD1iPVrq2x1mBlbSpr4RYdDlpcqqk9RkJ+oXLovimTdZHI6cUCQiDFZYdmEM0AhPPsGOkLnSTWTC2cCkhVhcvmQtIWUHI1pml87oh3X5EKVZauFKjepAfWoekKVXdxhVuHKVKsVZ1pr74ldc3F68ovcTzh03YsVIbMpggQjlFdS4iopLRGpjXBuTSgViyjtQJQGDegFckcQAXI1xVWy6LDmKinp1lW3QCaSSaoOhaWQFGYRL2+fHVY1SSn/aHfT5m2NPPLO/fu6pClpedvZc7HeysELW9jHS6fdf0fsWtL93Ub49dPgrROnlzDtis4ES2utG8C081aZLwwxqRZ+ew4SmVr4bdzE4TG81hU1m1xrg11T1pql1LUmjymDOfz2bPtce619g11Bfp1FHME2g5kQjpPuMNkD9hw7/J49Y5HkSl1simZJM3k709RmHW8qcelHkjMi5xunZ8i6TuSSbgHFQsUvunsRF/UMGLqMs3EmPIyaFeouTiBd9BpDlUPShytXLPzy5aZjvD2zfjSpKZVdfVPN5CmrZyHcTT+8cdt7sd2SreOzCxa8NUE5+M094x/2Jo+9ufLm5F+33XXb3Bo97lXGv1bmKu9RkGqjY3NENshz7Mxud60xyziP+IPZwVHBKcENQYWCweQ1knd5cGewIXgiKPu9o7x8uZ0V2gbY5tqk5SorpAE0l3Ds8zKzFLyD7HfYvIvUaKptJk9Rp7U6PX9wxjhp6Z7XSOKqBYGli3AmhzOF6Ya6y37NODCL4F9QqMyNxl78LLYxdoAtY10/YF17Pdf18Nrvf2bKu7uP57J3zv/IXma92Di26u/vdn5gU+yV2PnYW7EPDj4DO90OGxsPPbBip7Oj3XaaG8wnzN+YZb4cllMjw9Qk9qh0QDoifS3JwbUk2dZGrR6RopZ7JDPXhGn4na4CTWNW7lmk2RZbo6kaNmad2mpj4y45+AiTr0hkNd5Q9zZSkos6sMKQforpIoVD29kvNS88sWxQ7M3Y8qbvX2I9x294cHL2LcuXToq9qRxMvaXhv3fFDsXGPR6o+uHIjOf/dK3LyOvFXsL6XkqimdJaU1RVTFBWK+xbsbDFpMmLlaidZnCb0loB4YbyYIcJldPP/DhJuCP4jCjhl5vUl17iv7zM9zYNUQ427eCj9bnWY65hYi52XfTjgMR+Fl5HJDNWi5UskmZi503sOD4t7BuzSJMYAoVktSKkwCFZ4ZCsJotkWWo2+cxm03KcMM9LjMTTAcFtmZu4VfJLG3BAqpUYyxZHJUQTUi1mSSbmMZnZPPPPZj4QY5vMOWbONvDznBMOGkzjj3KxEFouEl3JbLKwgIWtszxiwVHaUmg5YDlikdn7llOWnyySNMUyF/UWTfg+PxQVUYtcZDdJ4JXOp2Y2fVAkAs+rLQGmursLF+7i4lZBp5pKS8BK4dOMGn/EoMKv1cqLDL9Wa1p0mI2rhgYgPlX/QTO5SkwleipBFR3E4xLBfBZiyrA4Nf38A9j/IVObNvFT38ZkiKADzhyPCxlsJlIdkIEX0eHZ0R42wsp6eli+lW2wPqpHAPmIwo7KTPY4PXdy2ce5rFid1js1xadpShJ8207nUSfn5c5K5zzncqfMTM6AM8dZ5JRpkjZH43yAVqtxSWOslrNyJKoD+BRwVuXMKWvcY4WnU2SPrDidZPZYNb6EtCXmqMl81MxNfnOheYC51rzBfNz8tVnTzNnmA+Yj5vNmRfDaQUnQQ0gxnXxmweRmC9FPcQmrcUe6I/cN5kUisJsSwdhIRISDitZRH+9qnbtgruDtIuNgjFc1ogNOxGHjhMyS/ZGCQhZSHQ1ybPTjsTFqA2vLbuxpllMjbCzj8hu/5suf/RpSDv76w2OdRqyXLuhxfAvixAXw2IYFV0f71sp6UFjoZLd7mNByuAcTs68NWHIsnCwWaxJchNWTrTKTizm4a5Fqty62RU0qc5NfncmTbbrljTP2KLy/CGYihddV5ozh7yt0zWCGd+BJPrkDc4e9oUz9OWFoC++w7Nlt18d+jf0jtuKll9h8Nnr0sqWxOuVgyvRnJr/xU9PjiOe28tqQ0BHxrPQ0/HgWY9H6mnZYcyYz5aQznp1UmDQgSYLRGgcSiY1qx8ozKzPnZUrz2rByDxuFDG+EbZKNS2kmi2lpepovPT1tHrpOSWdyVno+8kKRN05B0j/AMsoyBcYknnAgb0+3pMmutpXE9EQP4XZtW3mKT0/vs32FvgG+UT7VQj6ff63Lm25Ks8j2DHo7K+Vtu/p2hu9tbzTbPpO390675GGSOE36i42oAA7pYaHCbRyIWt6XHIGMQEJGri7iChNBQ6R5fp2piCYXT/rtkLO3iyCyyLJt7471h9Y07v9qe+y9H2JvxBrNDfZzjz/82rsx99+Z77uzrINZtt29atbUcRVdcobvWffsL8z3sav+z3fNnjdj6ksP1n916j3DH3eG7uzX87dINJ2UtaokeTS4GE58cRTplJMUyWSWFos0KQBnomdBrwptaBLuIKQ/2AnJ+2OpL8cy5BWK7Zeziu1xY2wL/O9A3dfviF6frRVqB5CTyRKzmq1LicG9Mlk1q0sVGQkfPMBGM5P98JVms2QlFadSj5Vc4gDPJT3rm0tf656v1szMbDECh4hrTk3xK/DDimyzy4lVviJSuO5YZ3GFKMKHFXXHgktafF4kcqmvkxPSMEOF3SwCMyxgysCmhpeb3jjNenfr05Hdh+zsyaa1fLp0Xcw76PZhfDw4NEQ8o5J7UYhy6Vx01+iOTDqexYqyyrLKsyS+MYmxndZvrHyueApSZim3NFiOWpSAmqOKp2nlqsLXBXFWTxuQdjzt6zSZa2n+tOw0yVRpngffpAU0bmZHNcZTpI4IMf7c7FyOM7ukhNdtdOx0NDiOOmRe5mDM4VCSt3rXpbg6rEP24hmRzCSH1LstW0ZtazKSa0x+5MR+f/tlpmjnjEreyTS5RWd1NdVzGtepEuOUXnrmywST4NqMLwf07wOEPo9tTvAqWh6FhMLts1WcU3CYLGUFhT268JYHnG1YOsNpJV+Voc1QYDn0bIr1qXd3vvTM1zvH9rJ0m3bjrbXB+vTv333+yQ7uV1bFKmdMqXmj++8371z5x7+mBZ3Jv+9T1mnkHP/D26u33vHDJxtY3TV5hWuuHfeQcXZZAd6LPMJF6fRCdLklkBLgFkuKhb+S9n4at6Sl4IOlMGhGII01BI8GuT/AcBgokpmfMRbQWIN2VOMBywkL91M2jhTHSQ74c/xFfkk8Xyp0g4nkX8ddFvc6k9fuMfmYzeQL+Db6dvpkB4dpBCXfMiXF2dtWY49mKLfwNvZJLRnLl4d1r2k8UhIPP8R/gm+Co9WUyB+bj/HGsyJx4MsOJ+mB9eJ5rw1jvScebDwXO9PwTKW8cx+zL3p4/R/vfKh3jfT46tgH38f+K/bhlhqE3LMXGo+/9MxHnz/2l9WVhv0NjX8tfQf9TCEcu+Z62AbbAdsRm7QBBwV5uXxC/kaWTablJm4agOzFz3LczPFn9zqLK2Wbf53kzUkpSuGGO5Ts+gMPJ0+pIUeN3bdMjabZb+Gp6uTLE2M9UJz5Ev8ZWx0rYkSrk63+5NvIjFVDLQql78K/bPn6wpq/z9v+berjqfPHbtj8wL231rjZzDeeZXm/HPv3pF2PpE6d9sELh9+vqdFlPxD7+iIh+8PRZQPSGC8MMFYIejTpRBJnHRH3sm0iuZfY17CgjiJHTvYncybJXhnJgtfv5VyyeW3QjwPqEZUX+Vmhm/H37Uw6gcxBsvvXWV3crRuUlsJsWoo/JTvl0ZQDKYqd+5y9WQ2lwKAyqJK3aWVQLgj+MpmPa/6SbVyL+bR+6zEgFM4Pq+LBjMsT6e5H/nCJ/KUvdsoTDr8c+/Xc68/dLO+L9b5t+/q771593crarSz/HJS54xru+OXFl1556auTB/54YIzOo6HCPiB7H6XC+8583cl+wu7nIttE0DfZkBCUKawoUBbgGuLJEfm4LFt56jrxLRvSU2tgndu1QWOkPSyvs3oDSTlJRUnScQcrwtF6mak3To5JyygVSZfN+H5OsnO2zIEIcgtPc7Two0nnxSuuMxXnzhjHpS/HVRuPvPWcIsEWgycVLKQHukK/0BXSn4K0YYaqaHL4winrk3dNuTvr6c7f7/vv2I9MOXPr8fVPW5+sWrzdzk7VH5w+w79jL8uK/cqKJv90y7bVu2p1G8Cxgy9CbpFMY6JXeTPNjgImrU+S7ZZ1Ls1p85SbWLaJmTSqBc8CmtXhstbk2IpsXLNl22ptG2yP2hSy2QJ+/WlwRfWZklddSLMrDusJUUlTSWnTq9hFLhMPd9V0FkkSiVw4P2KoOl+UWtDjmusbG/dt3dpm6V1DlRe3WAqnT6y7MFnaVDd76J236LLqHxum22ka5TBLdL8pxH4O4SCSwUYE2GhXlet2l5RkZ5Psc+x8nfmUGfqbxc5nMS2bnc/G0UJ8e+lh5z0oesTJohbZDRePsTjzusIuhI422W049wbCAS6VhcvDfED6qHTOf0phvyC/Tgmv87tSnc70dW6PeGwyV5MspG23rpPTtPaQb3t/++z2kG9mam/nMjLViDbZmpRGmtZ+mS/aESbQwfebmHLREM4IUQtTAMY1f+Xc8qyg+dGBftSsNlxjdYW4rM4SjxAQWvxCLa5m+aruQXQLyW9tIcnSd6O/2rrv+ycs82fdfWf6nL3Hfj174tBt8o5YZPbeXYuW/Pkvqz//bOHW/anDh/9h+711rPu3/2RFGxZfmHHg+HNvvP3UOy8KPVmBDFA8a/HihB0WXzE86j7gls18CrJk1YjZckAukrl4mugS31n+Ixq22Auskp2UdU6XmTzwCTZzjSWaBI5oScxLPsuk5lNDcyaNs5fQ/1L9Ea3Oh2phATgEGJvRj1UFQvGTpNNPzZo547F9+5a8MOXQYL7lzjsfeKXpEBz9D2uGP/6gHgOx8F5Ys3h+lxNNUlSJO5En1URVxsyVNSYT0wNSIK9EPLmivNLSRv3kHMkXz/F67cNLznrtTUrsPzFWZtTNJebBMIwpGEVuHiXlOiy/1BhB9EZrRvDIajZ0ty29G7WLr3qNJ+JBltkQ/yZq9gYKRDJPqcZVcoGWiqsQuPckLkKiNsNsLQikk3NdiroumqJl+DOyMyQbz3AlOX1W8qQLThd4Uwu0dH96drokHnnQqXRLjTXZWxP1nbJaJ4V7httZayZ2zWlX1o5r7ZjWrrDdlHaSSfC4ukKs3XXd2ZSmw66mV87qjzn0o1ppKYrgDEKXp1g8xMrNJdLvNn/oYjFClbu1gJIi4sm5X398rmbv811fXrVp34Qbbl23b5s87Z05G9re9s4jj/AtA2/5/f07mjbyvSsnb/2o6UM56/7dFRUH9+7V+V0n9A1889Od0eGvJDG+UkLocX3j4jzLle/q55IUX5IvyydJmo+ZjISxUpXL9O88LS5ml5xWZZ3HZXOSFQyxgx/RoFC8IBQvYL1U8ZCMiMcnQvOaH9bhBEst59OE+jlYYnt6+saFBs6pHLq6BNsqfnTG8O0VAV7z8O1D7rmz6ZCctX7UqJJ5y28VeliGWJOLvVixm2nRQaNsU8RDtXJPpWeeR/KsI5dtneotc7IBSQx2MSVpbtL5JMnkZOJE/7NTEkfseZJ+SpWcy6SkZeZoULqFB8yTL306VWF4iArj6aPuRhKPycVn628uePFxlhp764XXYidYm/c2bX2qZkX9k3Kv2DexWOxIrIlZWIgFmPfC1V8ePPJ64xuvNxo5ZV2sUs7CPlzwxG9H7z2qndC4kVS2yicf8T7l5VJDkNHy4MbgzqDEAkk7kxqSxDYC5hyz+EbTr2arU9S5qpwjM8RIOwv4jR+YIG+0sCBzibzS4URmGUVqmZoSLb66wMgvmUgxc5Bw8Yu5ZRvklum/zS2rDUUWD+sM36qnm+IxpGBV9cUMw+Bc6xxTl/IlX0RGJr3+zg+xsy//dYqybztLveORhxYueWSLtPnPsXd+jDXFji1v+rfy7IoLn71z7OV3/3nwFcNnLAPTXgG/nDQ96hDfC+bAQZbJioXp7tGLYKtZ/VYcY6yag0kb9G+8vo8GUzMKHC6zU8NBk1mJ3BatxhR1mSbxFnUV50z4mlLjeV/Tq4i+ur8kXauNYHvRFPkrV/eJTqrct10eWTdGlndkP/Vw03ty1tYG8fsf5Ir1WGM2C0c3a2FGG4JsA05mUD/SFHbe+OJzI7KoA+2PtOe8Z+bATIjXc9TD+9kYz7cxyZTOTOkmi6k2I92XkZHu97OAxHhVxu0ZXHo0A6lzBuOKJcnyikU8r1Oc0hQLQrElI112mbCCR6R17VwwYjf5Hvavi7q8plAglBMqCsn671+8POQy2wr0H8GE4AozTOkW2R6imuzslGV2X43Xq9aEoh2QbueEWkdZ2AQ+xMOFxNeBxnPbL/Vg+2XF5d/Ctjx2oLHN8bflNJeIvr95+qC2/g1LF5bdhRnfGfr5h7Y5lVMWT1q389GKb156/YuUJx0rFtx2R89RGz9f87cnn3sviV8YNap/n9KC7Nzec6vueemvf02dOePmAV17pmcVbJpas3vd/br+hOM/8GxlE3KzEdECuavZXeDS1mk2ZjiQWhuO9BZnsm899zrLaCMMVLW7a0xWDdZ1QJVIVZvTspJXEVs/qXiVkI2VljRWVOjPJnKzsAGsWaRjOM+J1EycOAp59tDC26YFli1D/Av1ye7AXdcv+QO/uY6Zpsfq6prWDu1jMs5PtdCdU3IWJdHC6OBJHqY4shzcLbkzrfaCEhwphMKL7xd9XAuYLQWaC4kBz0RSwBWfxenSDw/Mi6ygxhz1C9/sh29ONl/im5EpHdbP3SWe4uakIDfxfI2ao5B+OhA7ibh9hjjcEenUn5Xb35v5bOm+1D/8bvK+fZufmvLUvfy+pn2rbv/dqk95YeL5Czw1+dgt0SMNFib5Fbgm5kde309j0s8WtsHENkrsPCHn7+jz+mqNpzJOrvLlXqfP63U6TKppuc3hs9kc5o5Wm7XWYvZZLGaMQo+q4uQkMdVqcTp85DzgPeLl5d55OFnleIu8ZV6JWbwpXn7AxsptlbZ5NokHbCK3LrPJzIITyAnYl9dmNnHm8Ynn2qNoCj1KCtwvTinScS8r91Z6uddqsgQs5ZZKy06LYtF/95VhMZsskmbz27iTbA6nzSuramZSstp7YteGZAaVNr7SC4pU1B8J5BWNrcBFhVGuiIgH4CWRyLjEA43EwwxRierEk/DrcmsTvwltfmTbmjTfMr7dbT7AVOiPkMISctaw/oVfoTfiTfZHvMqh2JPP7GQ58nuHYgef+brds0+vLlHlf8lZv37EF+02XTgmZ11oWM2fbvrbVxlSN133QjgTfA/Z+Vm36F9HO9g0hY1wsEfgRnfCV+HALvVDwj/Ay6jSy454cfr1Purl5HA4vV53st9e62bl4nc1MnM5/eSU7VavyVHuqHQgLXJ4zNb1apoIv3yAk2U7H3UecB53yvNkxkdArwego3DlfK7xdHQnNdAJUrjZD38GHSkTP6dy2GW51svKvMzkxTnDa5aWw6U688T3CcG86mqd2QjX3fOaOaxn9rqJQiwR5FyC4xXjqtmgevP15dFku99fSzIUUMZwya5aaJXT6Wj+TVTLt603sArNUWI6bDpMzV5rnP7lm/GznApWbWaqJo4OeUx/wMB0IRQUeqXvY1WM9Xxwddd+nh7dJ17XNTb9pV+Sg3Iw+1uI4jWu7Dvp2+Waca+c3XRtdNKtt/DF+vewzBZfoJ5UNyPOeaGmN0Qt4gGkYlMkr9Yg5T/l99s8jmRRsngdVp/cIHXeq7gI5CkF2arf9LTUmcx5Z7pH8jzFwmlHIt27E0j37uKie16eu7g4L0//il1/nqqJL7ciUkjSv+MKudWT22LWbTGLvI1v3i7zB/QrlW2raxrC966V6tjc2IqmLezNWA8BsWaZQvEqbbO6CzlZNnWmCLNFO8Jlmk0ma5e8vIxu3btnOB0OJSO3Y8eMtqFQRk7nzikZWe3akVVR/EHs5UBKSjZZrZ6MBqkgaslOSUnvmJ2RFXI3SN322VxwBw1S7h5n94Px76m71HFPNy+2uye9s6jNaSfKWR1hqns75obERVtrg9RpD4E1XaI2RWmbm97NqZrzXHlo/1ROFnXpYnoGI5mkToJRRd3ziouLz8CEi/PO6O/GokThDArd84rEr7aKhe8nna0RNxjZvbv+k62I8dGMSy4AI9VXneJJr6plk5eJH8UWhtw+Q2Mi3REv9Bt+VfNyJbudF0fq7MJkvj/92rLt10x5kPV7WhnQccrqYGmsoez34/NGdHtuyL+GbdvGOl3zkLVgdF3s/GKWPEP68N57/l57Tq2e/cKzv3wrZ8+u+B3z8OWxnZ//V3H3V3+9L39aR3X9dfLQt1+8io1n34SnHm06GZt46Ja32KSmdSPZpkPDv9J/A1AVn6vVq3UUoAy6imJRVxu/n1IknO3sHrc7v0P7BqloTzaB1/uIhXp2EWqYV9wtxWcXQnQ4uuVly/mqEJMq9djHFKWwB25Es/P9kpSkmNq0CarZPbrYs2HRndIUVbX4UlI8aTan023J7Nm1LYT3ZKceXZIguD1BCL9z1ObpFIxYMiOewkJ72jPwVhaMbYFIoMjFEcFwXWjiM6JLxB0xPiIJaUTy8LpYrxtCXrO0UWw0PiHo5j6Jlt26Zmnts1WIpLB9tscLY/EUQkghLiVLYa4l+z0KxKjmsuxkPUSG8kP5kGx+KEnVkkLSZ5OfKKyKPT6d/f76CYfnlnzOKM+7xVbAhnX50z3TX+wzbML1tfIbMe+MUe9cs2xESSg2kW1+7I6m0dL42W+zM2cXrAgf+mHpVw/1688irOc6Rks7zP+s6fPYmzdLmbEvfmnXndmvbpo8vHKnORjmC9a/u71x/T2fvi3+taVC0+PrtQb1CMJ5d4pSfxrM/hgt6Ga12jtHo5JqykCgLUp1Jyc7U00DBgwenJplbt/ennpN795Fat9+/UpwrOdFakbOwGuvFVnifrvTqfYsLYJhPlVSktu5bSoEuj8tLbdDTo4qZOvPzVU7WHum9iy9tr3V6nbj2N2tWyef0iB13ac/DxO2KPUT5tmvL7Sh01Olg12DB+TAFvd1cNrt14o71w40N8Cus8KicacUfO7v7OrcuW16JopRW0ZGp4GlfSXV19vVG+32D+jgveYab95BqSsF4t8/GQikeoVBe6WO5IPRRoqFzQr/d1HQogpUGDe0otnIGy8aeELsHvEFq64xxUJXdDNGd7fuSFFuhLuIuCOtbD/x4Y4kLL55LMyiu4iENjGuWz6MXYXyqEK3RIWhW5ECr59rIZ/m9whPwHQFK2DCTSjtEwpmOIYsfRhtbLsbH+zZvulQxZJF9soVPSIlw0sn/Xvp2Rnl9bPTjn1mVjdIVpbhld8acZ/atensmqyBVVkTpPx218fOtQk+dHWbMN+9rPaTOaea1vY/P/u7PsqC2N8e7XbzX3/aMfDtHxeX9SlY0u3gPyvfaOzbh/Vj9rUpbGNu7Oelp6bKOE3+nHf1Z7ELsQ/fKUz9J7v91y2pQzuzCSWqS5N/d+GpYWz8jtF/uTHW9N20//ocKfTi+GxtrboWOXcqbY7aUpKSKCC8idWRcBL5exWFRJGkHvsZc3gDLqvQKp8NLsKTJElexZSS4letVtXjgi7ss9stHj88wwFEbS88SOAZhLsWp5AXEdLUZZbnjlzmD4wgCMcNDRGu/VJbb8d0WycJJix+6ZEU4kyWQh7m0dZWxZyxE01NJ+fw8iOxT4bFdvAvNvOTsYNjYj+wcHnT4h7M+Z1at4XR6YOrXnwJx5p/r2PDY7vXxayxt7+WbbEbYv9iksizdBxe/kRsnLPkJ0o1/rbOti/SXxL05fITz5//tqnCk2Taqv/jXqb30PuYVol/u+vxnv/2V4cnKVHf8prcYEpUib+nk8Dj/DgNl2eTHxiipdN6ZSSVI6Eaw3fRAmCglE595d1Ujba7cd0bdL/oK/4GD/AFUAqMBjKAkQnclKDlaNsg+ooxWjCbxpoyaLYyMh7DfBuVI1QJbEd5m/wFbVeLqUpco98hmagQ9evRZxNyh82o34b741G3RadGvzHo11ncQ9mmraIgqEUA9VkYpy6x35D0IuXLFD+LvYj1DQFWYI6hoAOBoWjjAu0v6tkRgfh23K8VZcxfg/o6oCxBh2CcZbhfin5hXNeinIp1WEAdQAjowHdTMffRIdBu2H+5vpZd9DnaVyf4JtbBRRvBN9wrVU6hzTnKxfUqnf/gvagTa5ciWIdRFwRCwBf6PgbHY7p8dlGVjtPUWfRXd9PiBPLBjwcMvv8WQtN0WYzU+dgCjNkG+Csvjp8E1dCGN8vhcmBdon64LovWMOSxBfOvTez3N4D+9U/I4hJgzpRW81sT7ZvlcCmEfn1BS3VZtIaQhegDKvYqxvgNxd7F/P+BbldkGqgc0dsP1PUV6/s/UaHPQqf+JyrGFfJLjCtjn29B79aK/YLuAv036EHs/SD0cIywC+AlldMhaZWuv4W4v023E+iqtFjf8ya0nZKgc+XZ8VfQ5hFxzevQZzZcRkKOgi+/oTz+q/Ia7RBlXa7g7eUU+rIUtjZEt1XYQYLOTNBi3S5hG/8TFTar2w2oVGpQcZ1YQ+3/lur2fiRh77p8DbsXtnc5FXYMXnyhnIau99L5XqPbjs5/yCoEWW/TZc3VoXoboVfD5TlUhnGnK+9j7e/Dd3wRPw65fKEOh6/C+VgV/uIcreBr0SchB1EW/kD4Dcj2/WZeqvkJ/mWj3BvjntT9aV3Cl1XpNvAOjeffUX+dP2sor5lP6LtNPoe9X8B64etUmXrLp6Hbif0pKg0Dxsr5NFI6rvs04Z9TcT1Y+F0xj9AfaT/wBkVEDLDsou1mzG2CTpt6wc8W0yahV6jbBt4Ke65rthHs+RPg42Yd+N/KyLCHS+1N+Bth87+xhwT/EnP0baZyf8QVAHq8pfWaL/b7zbjDhS/4rb1fap/Yz73A/cLOgJ8u1/OEPqcl6HVX2NuwRPxYrQNBuFVMGdjsx9RCGi6dREw9iTEXQa+yaSD3xTcm4k1/6NhAoS86jHF1vWTfxy/w3tCtD6kv4JAmG3qs+PSYtCwB6HG8Tvdxcw3fqDTAR03EmHMxz674qYugZToWC8S/kHshhvUy4hg7GB/J8+NrBQWfRcz06Lr3Cmxorr6X1jHOBci6fS0DeiHe9KJ86FYIGCiozodv0S+fNor96nvE+sTYuGeShut7HNjcRztoAHo4UFlGYekz+IPnKMw302QBVhw/zQTdHP9OgM+kychRZBG3wZOx/BcaC7oMmJ2gowU4ZMJFLF5Nc4DZQH8dRgwNshWw112ULmKpyA1aXY/U6ybTLoGW8RrghwV6AaeoBm06Az8D2wARg1XhA4EOWJuDFWP9HbCPbmTim2B3J9H+MhA1OYBriC7UAfOA1US/PgP6EOqngu4BvkPZC7rBaNe0EhS58YVJRju97UhgskHJeXHcpiEGLnxCFI+gLPocMdAEcVx4BXgX5XcvzifmapqC8lnQiYn5ngPqE+usuzhv6zXr626+joNiLb/cj/7LjP5NmYkx9qE8wmjfsv95iTlvBN1IdP5bop/F33LspaNYzw13G7lo/DVh16KsbIvvVD3xnbwGtDh+VvkZ1BU/C11b1ZxrypNphzKHbIlcM0vYtLBz4ROF3xGxoznPVCI0vVWOuUX4ZD3PFG3m6jmoRZ1JY1UXcq1iUvW4s5nKpPl0lbBDPdZUwhZRJy817EbEEXFfqmnJIceIdtIF2K+4P91oJ80x8hllOmxoDWL6EVyfRV8HrjEmYk6Z3JkciEe1iFll2GOjPpfIgUBFnT7nd5Sn2LDfY/FdOs2GXyulbD2n2Yr4t5rGKisoolnAh13wlfORK8xFXTXuXUURrHesbIHfvUCq/B5y1ZO0GDwok+soG/vwy42w+zX6moeKXFnwVT2NfivAs0M6j5rzFUnkKabj5DF5MNf3ei68HT7Mgjxjuy6fXjqfN+p9q5CbYCztJsz7vh6P9TxS77OdPAkZ6bIz+tMWfU9CPj7xtyYRB0XOeZq2ij1pY2i7tgztyxEbXGjfS1/fEJMN9Ihx/pHXI7aXgtcu7GeXHpOHKqPjv0i7sNb5qJufyK3WkF+ZC1ql++gyeaTuw0RuORRtRRwOqesRb8UZQLTvCx2pQh6+Hrn8OfENCOryAB91U86CjtRzUxEjZH1un963VOgF+GYSfFUvkF9dpM8n62sQ+Szm1Y4hh4CsFfFXBXLjF7RN0ONVery+IMOqtS10SF2B66twv5G4Nse4Bk8HY63FQn91HWqOdcgpdP+eoJaraLxpwcVY2Dxfy7yC5+ng0yKcR3ZjbQ74N1C9vAB2uh9rFb9kccUvYI16nitkKvgq5KrzFjLV93SRkrQ4fkFNx73B4Nd36LceeV4EshqDuIszorwAsU+saUFiDSLOfEG5gv+JPDC1FTUJWWjDkRsOxH2h45BJKyry3BXqNtjTSHI0U6G3LWtNrA25mrCZ2pa+gs9XyGlacoz39XNyC/3N3gWFLei6PT+RU8y/KI/f5CK+RI57OU2sR9dt6BfqI9DlBuhxKdAf5V7AbKmKCkH3q0Svoc1YlLtJRMVo001ZDxsopd5oUwwq+om2S9EuH/eOgQ4EeuvjltIq1G0FrUZdvRhfETq/i45J79Ne0PdRl4e6DwGx7kbMuZnn0l5gF4sg9iKPSVynS2Mw9y7wooqqEpiYwE3AZL1ut349DBjNxVylNATlMcD0BK1MtHXpbXLJBtqA/Q3W6wm8NeaoBq5KjF+OupHNc2Lc3nobovFAFcpb5ULU50KXT9IRYDBwip9kImZ/CLyPsgt0J0Aov2/U6fW7QD8BgihvAp0LHAK+T7SZ2aocTIz/vdFG7yvGo8Rc04FVBvTrxgTOGmhuIyjrj7NDN1HPGmltoq4z+9roK2+hBYgNQ6SB8N1B6OCLaFNLIeRXxaIM1Ilr2YH7hWj3GXJvo76X3m4fZPU+9GsyeLUCer0KsrAA6SjvQjv0UzbRWsVFuXIDxkmAHYSMBuvxQDwTaoA+j0eMmcl+iL/NfqCx0svxsKDKh9QbdJuai1iTTosF1cujIY8EpGqsS2BsvFLAvIqGCliGG1Cfo/kCWPN0AX5VPKbtxDiTcS5w0GJlMGxiP81F7NPnEHOLeQVI/AVD449auy4ri7/UqErHmQVnqzeAMmAosBQYA1wF5CYwUh4PXSmOfyj2Ki2lDuJfF2CvJ4GtwL0Ya6xkoW1ibvmmeFjtTL2b9/o/oTUPLkcLT/4TEvy6BKMvvW7m5eX8FHwUPLwSdL4KbKXFprm4BhV8bsHWK0PIoQWQxSXYgTwKspE9OGMkIHj1nyDkpx6M/9wsSwHo6RoBoQNiPOUY/NpcKuRrqIzXkYUfgi+6QBHlIPzVUuzhHcSvIbRVQEpFbtmIteEey41/wXvHdyGmDRSQhuk635+9jPwrAdH2CvohaC/gxdb6gfwWeXHMz0cjR6mjkdoA6EyfeJOOp+NNyr54kzYGdCquv7gUom1rIP40KR/j3nOg6aBNoEuMMXS6F7gf9Upi/F1AHurqQJ8ATTHmVjJAnzDq5Bpcb4z/Kh8CXUa/U1yoa4w3SV8YUGYBxw3obebTdfKnoJ1wPTbeJM4T4GtPfk+8yUDTedQNQk7fC6gAZgK4/vXPifNCBO0+5PfEuqDtadCjuN+kecVfg43h3PJrKq4LgH9est8lrfYj1tt4kW8Yc6cA38FUOYWpSjrrDMxQ0ul1UAINAI8C/wLWAY8BP+JMh3NK05+lWbQceFa9Fk7gauRQGA15MCl54l8RAi6aoVior3I7coCfgT00R80HrtKfHc2QP6TRSo74V0d0i4zdyo+j/D76KcgTsuBHkRspzBhPlOXXEE9n4TqHpsif4foV+NSXcO2jG1UxP05OyI1J/hk4lrgWf2nnqN6WlICxRqzdw5BrGmi6HtftgBCwCPw4x3HCEkA+qAP8XcrOxZi+jmnGWM17Q/sf+RMX3pA/pzuQm4zFem7DPqfhegX2tRiYivpVoNXIMaeJNek4RtfKK+JDQPOVjXQj7s/gYXoX/aqkiThT3CT+PXnsBWU51rAQ800zoCQg3wq6PHENivOXziMFu1CWoHwXcAjAaVU+CAwHbgNuAHYAhw3e6PwR9/+YKP8xcV/QW4DbAZxs4RNIKQQdZvBY5/Mxo16H/hflYyMYeG8g9jp4cyoB9LxQAuwHcDfWRl9rTWJugUd0OgPYA0wDpgKrgDnARmAg8EcAO4t3xhi36jzA/rVsKsa496FuDfKmfTjDXWsaDf7uh70KLKAFkNcC9Q/UQdxrhmjTDGkIDVDzkC9+iNg9FzL4BLSRrlVqaZa6kCYrnamfkk8R6SGaJT1A86XJKL9Hs3CeOCr3Q84wDTFuDY0GnwuhYzfIa8mCnHiyMoQmqzaaLL9AhdK/kJ+FobNfo18p4hLuIS+vQk7wANqVy3tplXi2gHGn8jw6aiB2b+I5wFTgA+BNXN8Me/8O2IH79wHpPE98vxWzq88jz81CjkF0EDgJvApcBWQl8D4QAf4FDMB4Ev8D/HImzUMeMkK9jUYofeDPBFxACeCn30HuvwMfJ2p30kTwZZ52FO3fQ925BE6g3ZOgf07geQB+Ebo0Q/4AdCt403zv40R5b+L6ncS1GONdo07JSlxrNFv+EbQ4MWZiLh1/Fn+dt2kj/wvdx56jR8GP9/iNccGfOQl7niJ9QPMETAUGwCNYx6+fs+eatras5w3q2rJHrFM6gdz3BM5MvyAPslGOfBy6WYLyzVSCnPBRuSf2tFD8PycufKX8Nw0yKdTF9AN10X6k9soAutcUpFnmDyHjt2iq8neaqp6jIjWVJgrYk2miMgL69zDdKz9G12qcempf0BJ1Kt1rGYP2pfHzijUexxhToVv3Ku/QvervaZc8BuXnSFbXYLzzuPcG3YFz71TTXuA1mmrpjjVOoHvZhJiFnm96Ts6Ou9RlcZdygRYpf6DZ2g6aar+eFgGznT+KP/HXlKyPg7WI9aoSxj+N9UykexFTrhVrF/tSN6F8iHYpL9IatVv8PD9Nt8Lub+WH6S7lZZoB3z8DtnytuR/OSDV0q2k83WqZSLdqQ+hGOW5A2QtfeTXdKqB+hbNzBsachXNpAf1OuxvnH3FvPvoLjKFbzQ/j+mOsJRVzvYrybgPqPFqiPI3yFrq9GdClG+ULVCaAfd4qAJ99o3mr0Re2cKtiQ/lT+NRzaHs93YQ9l8HXddMGUqnSnmz8ZmoL+PiX8S3KnPgWfig2XtkW6yc3Ya2A1oNs6usGtLfIptxIv0P78XT2l4dt57DfUsSfp1lf9Wn4nqdpJsobAT/KC4APUd4MWiPK0l00jnMaB7saD4zD2gZpmdQHeeI4SzmNgy8apJwFeiTQDzFyJKiBIdKT0J8mCsOP34I4OlqZhPpbgTKMhTbmsTRDq8T1WNjnreD7jQm8BzndiDYTUE4A9jxOKU/gD+izGGs5QhNNb9E4nD9mKFMx10CUK+AThsPup+F6DOgkzPsMbGRy/G/8YPwUcp1T/G/xc8r++DkpmTorqdSdz4mPVSriY6Un42O1vvGxFgnl+bEGXh9/j7+AvldRD+Um2MO9mPNdGoT4MQLoD3QGqoAhQB9gJDBeFs/qJ4J3P1AFn0UTzQ/SRMtb8EdX00TrY1hXEurWwca+pNHg5URzPY02/4lGO/bjXhh7+ifq7sX9L2iE9jFojEZbPqChoq0pRG0sAPR0tPXPaNeAPS6F/ddQuXklxjmLtlmYSwMO0oOwxYmaScdoUxT3J+M+zt8yeK+0ozXaRvCnLa6rsZbV9CBkfCN4N1rDOhHLR5v+jT53gX4FX/MmjZDKiaSVyDmepqEC0mvkgn5OlLE3y1XAD4Ab+1wr/r8tTZvk6ThzYJ+mN7H2lzDmDGqjfoAx/4B5+2CeV2mG+QmUI7gn5uwE//oyzUT+0ksZRr3kT8BfQJSlVxDnZiMHKcS9ocBs6qXWQoffoJmwl146huO6DPWbqUoLov419D8OiDEjxn3RH2PqQF60VcdqlFcnxtgB7KNNMkd/9MO5eLRen03ZyFOqlP4oC75dlehTC3lvpEVyGGvcQqOFv5J+pnzp55jIJRyXIib4ch3o89Dh3bwK86yhTYijhfA1vQCf6Atfjtz6wma024X2yASbkL/FIka+LcaNTYDMFmB/C8D/yTjvbIJP26T0RlnAR1nqflzXwa6zQW+iXtpZGqZGkJfupOmK4Fk3nIdqaLAyHTypB14E73YD+TRGG452Z1F3GhTQ6wXeT0CMgzGUtcAmnPUFdtFcvd8mvX660pdmSnOQE/WnOdIxxO1jxNViln4p+FzQC0AHoDNstloag7PbbLSvpmLkKWH+CnLVNeJ7QP4G2uQBQ1AuBd0r+iuvGGuHr1+BfYxWFuF6UWK9NmOdWinNtHSmagHkBFUJ+BMQz1Q/MmjsNbmRhVDeDvQHDhngPtTXgKINWwzMxPnkOO7lovw1oBqgN0D7J9p/B+QboAtALcqmRFuO62HAEaAwMV9foBswB9iJ8Rf878DeMdZAOzCvGHcvqFibDKQCs4HvEuvMQjkPGAl0TmAtgP0grzPKryTo4gQVeOcKdY2tys3oddlYAqWJfq371ido9mX1Vcb62C5jHzpEvdjPokvBggZohQHBb6lbq/t1BvgcY08MdXwI6GBcPw4K/jNcwwYbqQHYaMiVTif2Bso8ieuPEtebgfQr1AUTdacv1gl56OMcvKzudEL+pxN7ax7HIeZXBtNInFnGwJ7GKGsgp89ojDoMMXgo7KcM+cMC5B4eqlV74/5m2LCJahXxu4bt8ZiyAvgMensauiaeH1ejfqa4h3ZZqLsA/l6AfxB9XFSmdkBb0W4wYo+oGx+fr5xE3r8ZbT+EHI6jXyXNVUfCHhfo3xsPRqwZqf6Cso06yKPhA1+jbuoPGKcX0Bt+ZjvuVaFcg/H2go4HNtIwZQHoTKxb/EZgAXVWC3EtY/xFaB+Ej0Uf+L5auQ7zV+nr6o0x+ovvZ5TvUJ6IffSHzcwnH3xXnvSRPke5uhY8sdFeEzGTiehb0G3AfKAbcBPQkEAH3P8IOInyYtDPgOMGZTuAZUY9KwWyL0O3y4BxWDhBO1wK+gI4BjyXoADLSKC5nei3FrC0Al0EzTGgl78Hzl56X8dB1B8yEb/JGJu7rtDmP+EjY1w6YoyjU4ELCSSumQxsQbkRdE7z/ynQRNJYzPkyqA0U/OVVuL8IdCeo2PcCAG2oEliUuIZsqA6YDqwG1ib2ugLjQIa0ONFmcStsS7RpLi9thcX/AXUJzEm03daqrhmt67clUIM9rAEWXTq/VHYRoo7vArZd1LsW3WvWmfkJ/btcd1rjP+nmFfRSn2fTFfSzlY7S5t+WBW0NvW77ZWsJXwrafVnd5br+0W91t1kHhVxb61lLuVmXvrsUbE6r3/Vs1H/HdTlt9fs9/Xd7/yf6v/xdn/itqvjdyW9/x/cbWii+t22+5sXxV9mR+ErxW0rxe8bm3ztdThO/3Vth0P+HvfcP7/LKDvxe/T7GHo/H69ZxPV6HeAhDWcIyCmV5CFGjKFqtRqtQRVFYVatRtBpFo6lGUbRUo0dh9RAtJSzRMJQ8lOWhlBJCKSWEEEq8DMOWsK6Hx0NcHtalxEspS6jjLmUnrtf1emzUe973fHTO94uwPZnsJn3aP85z3x/3vffcc+899/y69537LUv/W0t/R33gGvdTnj40ps/Sh/kWgw/4QJ6ftDS+j/Q5Sz/1UXF+C8X7eTr3pt1/4mPH/ZX5qIn/+6jU/MGPl6WLHxYrWPRVVmWxTouL+LektzwsfjRP537/w99/nPQjx19zUc/C7xOOTQbn8rjlD+3vud9/6PvVHy8t7x9iFD8qLYnXXCDN42Y+BOZjl8fm9hkcSnCq5vzcUYU8zmYBSLLO0QRHavfMHTUYTTBt8TgPhZr30nfvzR2pq507ajCaYLquP10nSLSeTDCS5uYdg1sGZxQqR+aOJjhSNam45TCaYLrqtXSdoHqheOa8P+YuJjhVtzTVs3TuQoLTCc7k8T4fArVrUv4lCZ5KoDFRi1NZHwZPpHqemDtVez/lVXgrwXfSt60FQHfoCF1Sm1uMT7UQf0QMUh6H9H324/fbL39W7f5Q3AOkeXcywe0inXtHYUG8dyS8DyV4v4hbsz0PiwuYe0djvhMcT7DTQO/PW5m3K0fTeFII3zwwDnbl8UrbuGdfRE1qn8bHKQ4aQ1dAtnNB+qydu6HxdIlGNzSezuLdNP7udLXJdKRpPdCjC7OKrjx+Kt9nkv8r1P9eXOxB0b0kCRL/eVv3NEiC6plsddWI7vnQmL35GM4RvU55N+p+FIPuavYGXM6GEig+Z9L9GZV59Zp60v2SRYezJcSGVq/IeiuOZOsrryd987GUTidI5SVQ2ele1UtJv9M46YRD3c6sVkHz1O5M/DThW9F1f63tJ9F9F88neNr2YDxpuGkc6JIEbUDFifTdiewH0/WQfTMc9m587O8TXromDVUXe29UlnjW9qToPpHlsipbUrc1ySPN+R6LPM496YdrE/Qn/fU5hZp9SU7Zl+hePO9JeuNyh+yO6vd19dmWBOcTPKnXKf/auueyXoU0dtpkRaL/7awz3Y8oVFeq/znpb73p+VNzl+saspV1b6S8LycdPSugZkv2RPVItjrppv2VndmiWo2XfHnuDdmTHdJ9LTV38z1DGpfYVjec7artyetaCqhO9MiA6hHZ5ZTnqVT/09Vd2WDtkmytQs2ebG3lriRj5TGcc/dTGddqlifcEj+qeirJHnezfQmHlvRdn9zNWqsasrZ0P5Nge+WFrD6lu2uPZ8MJngnp4gRP2/3ikCrs0ft8j9JYdq72aHaubiKPIX2sbjJ7TBqy6kVLsuoke+gzlUEWSUta15Lsxn4nldfy2M/ZJCN2JtiX7+V4NqXP1vVlzz6yNF2fyp89ntLHU7mP53zWYsu17+tuFPGpeXo66cqnK/YvkF5OaW0Beq3P52HDwt8B8Xn5u6yjdksB6d3edP9OSltC+nJxndp2OrX7dHYk/2Ywm1LwciqPlZZdct8T6usJ6Qae1+0rwXcxae0b8/fa5tUGlxfAff9D6LZguQrY3qrezudSbrtJbeqtvpNkUoWBbKPBmQRDBi8luFS7J6tXSOW9EPfFGW/JdM+d7vHK93ldzjrnYSDbl2BZyV463Qf2VL4P6FzVxjQWryTYnPIUMJjDY9mW2vGkr17PtqTrpnTdlK6b9HoeNmddClV9Fa35dVkdAXSPVUfK99yH5HnYdw99n3hADt9jmR8JdRcKqD5eseTPuuwc75GPB/N9kWD+eX8BPE/jZ/JjwFSENB4UlgBVq7IpBXueQ03fgvdLFoI45u3Z6dTOwQBF28vH2EBFQ1r3x1L+DTWrcv/IunKw75Yo1GzMxg22KORlXHG6LnqtgI+kf38pfFR+lQ3MLpaD0STNzQopIDtYQMW2qr3Z1QjVA5WV1QNzIc0GEhyjb8y3tybB6pr6bEnt+tSuIk9PAs1/O5XbNJ8+VjGmc1P5gILKKgYvApWHkpx0KFsvWe4v3JTg2QSXzXeocETtmwYFn7mcqW3/+SJ9AF6z9HiCYwu8V7t/9wL533tIeR/yrqLlQ74p+75ibQHz9ak9fTA9W2nlXE1w3p7FcofsWbvZ4leX1b/a7PpT1d9OMt+3s88b/EyAJxP8VPW3K540+MF0f9PSf5XSHyhA8zwA60N5n7Vv7H7+e8qljN4E/yTdL6p+LbXrteyKpVxfqc1KIT3TMXDkLzZUDiVYmWBNgtoEKxIsTtCdoD5Bo2QV7Qm2JVhv0PwQmPrzb8//e6H68tybBVQ+kebBix99rynX8X3M9728L4fy/H9RIdKnot/u+wt4IN/38Pzj1h2vc/+ywTyNz3u+vN/OB3zL0u+LDomnZt8urb+6vvB/fhyo2F7AA/eHDM4UMK9/Pm1wsoD5ssryZTMG7xlMGswGiLic/Hj4KuRtzsrAnufvPqy9Ox7y7kyAZbW5jyib0L0wtYUP8UjttqzN9ou9nqAlyRI7HSr2pfRyzYVsRQ43k7xgUPtMhdSdyNYkaK2byY7XzVS2p7RT01S2+rMHUzqeoLf6cNZTdSJblfSEliSbnNI06c2jtRpLejnbWTOYHUzXmndGIen9Oz8MUp4p8hrsS3C8VmMy71bUJ33+ssFUzdpU990kEz2e9SSZaGX1VNJ9T2fttbrH92bWUvdM/rxTDmdddcuTbpLS2tGsv/atbF3Sg1trdmVpPciOJehK68I6u95f+EEXfp504oFU9kBNkodSWQPl97W3svGq/qy/+lyqJyugaiZ73WBd3SvZeN3JrD1Pr2WP1W7IdtR2ZOtrO5OstjWX8WZqr2WTda3ZdNL7W/J0Z+qHV1L5i5Ls91LWXPtM1lf7dB6DorHE9bXPJpo/lZ4/mVLJWpLu2FI9nGSPtiTHHUjt3J6tq21NY6E95VHoSDr+xnTfnWiVxkfCoaH2bMJdr48lGm1LeY6kuvtTu95OMvalJFffT2V3JOjK+mp2pnyr837W+xatt+Zo1lBzLGuu2ZQ9W7M/W167Pz3fmmBDDt3p/dqa6awx0XxdzUxaxFtTmzWu4HSqrzP1eUu2uvb9rDO1bWUOTxo0Z60Vz2brKtdk66o6EryQ6PtCLt+35/B6dqq6O8lg3dnaupbcx9SeaLE/QZba2vLnAXVL03x6P7teN5A1pfTduo0Jt+HU5yPZUYXapLOmtrbWHsyeTXlacujKrtaNpXE6nnTl01lP3ROJhidTnpOJJt/JNisk2rUp1N5Pbbyft/P5ul2JRssTDe9ma1K6rO7Z7HzduWxTSi9pH6Z6tO5jdRdT3o1pLhS2ywsJTia5aUwKH/ozCZJ8lV2XrFp93odSWpnSwZROFnmqVW7amdLb9k1/4eOtyey9+s5n0736ce+n+4MpfTfBhnTdYt+MpzTpSVUpb2VKK9VPuzalLeabniryqI85uyN53EJNdfpez5A5YfNwR8qvctzeBAMJai1uIOGuPv2Ke+avfiOl58wPvc3uk7yo+k8uA16R3N+cTSR4xfDDj9wmRZyB7rtUf/ClgmYVR+3bN4t25M93h7LeLp5XqK6lvvJEv+y9oix0r1xObS/yVzwmBa8+neBu0V71cWcXpYh30DquJtiTYKboD+23HLRNt4q+ynGrtDLeLfzfSj/tI22b+rcrnkqwvWhPThONLzhq9WvbW6wNb1n5dw3nVH9NkrVrUn9WJ1pX3S36LIe9Vvd5q/ey+dRnrH06dl6y9p22duxK8E6CA9amGaPjEWv7jPXzIcNPx8yp4r3SK6eZfj9lzw9YfUqrV62OBJVprFfuszLw72812mif9RWQ90e9zYHVUsQ8aFmbAuwMkMZR9prR6obhdbB4VqHzSWMhDhtd9Zm2e7rIU/FkgleK+cGakvfz6wXkY3pUitiXPYa7Pm8t8lcuL6Ci0cazxkisMPpdNvzTdcUTRX8ojXRuVD5u99csjkLnyRWLuaCcdsvzbjF28/l31J7fLsqs6LT+3lD0Zx5nct/wOWV9PCUfHn9CDNTHjEH5XmKk/o3E/2QF34igvCin3QlZOFYlxKUQezUfa/WQOCu1G2msSj5fY3xKjHOKsUTDDlVp3FUmflB1vmhzPmc22hjXvD3Wtu4wrjYYLXTeqw2g3dr+/3VY///Dnwp07dP5r2sH/FjXRV1DlB/qmOsL8OeN7/cDyqN1XukcSjJIPqd0bdb1RufRRGjnxAIwtAC0Wxl63WTQa3VpqmtLh10r6PrfbzBafJvHiTZYXuVvbWUwbvheN9C1WPke69yGD4GOh8CYldlm15us7s323fYAk5ZX89ial98r6Jqosschu/5ex9OHQf8CoHx1q9FcZY8tVnab0XOt4aty8XGr75B9p/UfNNhifTod8h020LxnDfS58hflz92W96Th8L64XKdrzk4r95jRbYutN0cXuN9nz1QmmTVcxqwM6H7OcDhnZZ4z2p6zfCq/7TecTgUczhrOpyxVXE+Iy22Dhudmuz9b9JHaWXNaKr1Mnshlk0MmQ5y2MmdD3bNWv8oyuh4P/VsYAwuB9k135ba5Xfk5FQmqbs6NKeg5DVXXssYcjmXtVWfn7lddm7tftzabrs6yT+fQmY1m2Xc/Vwof/FTq4p9M14tS+gcpfXehfTS6byi9+3vFmVffTcV9kPK+r3syh9L1z6V0S9yvk58f1pb7d6YNd+0L5QsDNqa0v5j7Kg/ouL5j+VVm65KC72j/Kb/psPGpctEmK1Nlh3HrjxkrW+eFyg0jNi6m7dmg5W2xfB1G+167nzZchgzXUatPcVtvaYfhNGl1r7OxOGTftxpuzdY+eBe8p9/q2mi4DNvza1am4rLB8iELalmvWFt2WFmdRpNmy3fRvp8y0Lar/jNr9SE/9Fhbumws7bLrKcN3vdU9Is4Lhywdt+93W/237Jspo9eI0eeEXSusMb1D58gKw3mj9a/6Ilfb/YDRsc3KRpdsNfwYJ8P2vs/a3mX48l51W/iS6qYT1s5Nhtv6Aqf8usHKbzf6H7bnG6ysWcNZeW2L4bnX6uy0difaVDxe9H1Fnz3vNDzbLU+n9dFqK3fQ8LlkuG2wtnSE/tln+ZutHNbzq4Z3k90PhzZMWd+ct/K1b9dZWUOGB+OfcbYx4KvzR9c4xj39r3DZ6l1t+ClNVM+9aHnbDH/GS7+Vu8vaqPWNGO122nvmnuZ/1fqqJdTbE+ipa9WZ0M8Nlq/Hyhyz9rRbfn2+x3ClTyftuzajWbe4LHPBaA10VLflZ1LmfLN2bfZztXpOXWO2vfaJrKG2OesOsLFuSR5LtyI/82cwa6w5nk3UrMpW1VzLdtc0ZO3V17Km/PqJbFltd7Y09+HrWUEB8vOJgAXPKZr7Px9yRtH6+bOJOJdogTOJ8u9SufpdnudA1lTx1tw/r1w598/zc3bas+nK1XP307P7f9b3+Xk5aR2aX5PK7vUcH93/WLN87n7NM9/7fX7GzzE762ehVM/42Wxn/aRUz+Cp257q35TKGkj4fo/3+Tk9l7INpLQzP1PpSnG2kp7bk2h7v2o45T+Srcthc7pOoOfyVJ1MeSzNz2a6kTVTfn6uz9m55xZMH/KutiF7qm4iawKXeZz6skPVL2SHan8yq66tzapr/sXcczUnEjwkfeRo1vTIl9O4emHuvmxPc0HPjErX+RlDG7Pm+T7dlI3msCHNW813IGuum0r3QFd6vjM9P5yek7c8f9n9orFsdFFbNlGS2vWjo9noPLSm+1dTesxSQO9fSPnvp28TPPJONvrIlZAm3gbI6ynPU6nOZVlz+X3qq0PyjfQMuJ7w25O+fzXl/aOHt+eR51PZBvJeKq8+ldfy8PyLLiZcDyScr2XNj959eD7OsNI+qOzNpHIwe9xg/rpmLL1PUPeXsom6byZ+l/pNfj615c/o+aJVCZo9rd2VHXrkn6Q2b0ntPJueJdroN+SvHU/v/07igw8pT8fSw97VtabxZVA7nsbeV/z+o55puqh57v4jow+mjzY5LBpJ6eECFl32a+4XvZtgcfrulTQP3vZUXnOoux/SrzvUfCk9+0cL45eXATy3cJ5Fe1N9FwpesWAb/4OUrnp4qm17ZMjT2o2prqPF+/K8lanMysSjKvvn7j+2Jmt+LPXBo4sSPO/pYzMJ9nr66NYE+x5MH7ud4F1Py+fsI0fnhhQWSQHUt+hSgv/V0wfqG0/wqw+m32t95f2vtMnptLh0bJTkW1xA5crskJ7PV53vNa9Y/+jXEr1+Jht97Isp39cehMd/Ixt9/B/M3f/kt7PmT/5e1vzEF+buK3wqzedP/m42+slfTe8UvpZNPP476d3/kY0+8Y/T/b1s4okvZhP67lO/G/Javk/dT2W9lN7/cXr2C+ndj6W6Eg5PfD1rfvy/yyY+8U8T/LX07hNZs15/8odSGen68TTHPpHWyk/87Wz0Ez+WTTz2I+n+l1OeX0i0+6K1JT3L8f/lRD9twydT/r9WtIHrmD7+zxLUFO3Ny7Hv8+vfTJBwevTnEn/439Mzrcvqm6/X8mj5T3ytoJPS58m/lNLfLVLNq/n4Xq8f/1cJ/oE9e6aoP2/n00VbtLwcj0tFO7VtT/xGquM3irr49tEPinZ88lMFrfPyV2bTj/QlaM6GF/3h3NATf5S1P/mpbOJTaS489uVs+okvpH5I68Enx5JMujPbW7UvG0uQkUpWlWTeqpMFzKmMnJk82mfyNjJ3R3j+UfaAfQXo/yrm3o/3FV3ZYGWSYVM6otcJRvXfGykdqr48927Fvqw1wYoELyR4LkFngsUpz+NV9dnqihvZssrL6ft92VJNAd3LkvKsrG7Kuqtv5ueWPqU8Il9/+rMn5u91Dcr5R3rWq/wkrUeJp1Ssy2or9OzB4qzLK0kWys+7TPOoF5mF+3LZQueZzrdyORL56qPkze9bfiyTFzkXsnZZqitB+f38eZHb0/32B+/tXM6v1txI9d148D4/r1P/i3IyXY/OvVd+Xy4v52dO7sk2zN+XyY/5uZL9iZ4rjR565ufz2UTV9XTd6PJrOV11vala7/In/ZKnI94/1UNzddVX5x5J6bdS+q3qoeyJ6qsJLE1j/z+XrPKPk058M6X/nhR+8KQ7V/16gk8mSDpz5ekEzxXPKu+lVP2if5TgBxP8ywTPJvihBHVFGZV/OcEPW5ryVFl5eT5gaaHfqR2v4jXz6R0yP+MT5iNLul5V0icrR4t3lasLm6D6idVXmPseV9u36uc7a+/6Cz1S9fwczqVyND7gtqVa59vFfvfKpy32c5Wlen/JfKRJR606kqC+iC3Qa41vqNxQ+ELz/Ao3i/eV9+3dM4WfMs+TriuXFXnUD5jn1f1oI9V63m/SlxOMVmcVu1KqZ6xvqdZ4qOLd5gTtCYbsWp+NV2v8ZOJNCboSqM2wM0Gz5dXnHfkenCzpp0Ue/W/D2gTdCQbsXr/psfQ9y9dpaX11lv/noyXBxXyvd6b/JUp6c5YNWj1rw/d9liqOEwmuJ9A9yrcTaLtm7P1y+zazchryfcwFTusSrC9okZc9abhsCEA7G40WCm0GagOoNrxfSPC07e3abTSbtvZfszZdTfCipR2WbjD6rUqwxtL19r7B6NJg92uLdxWnirFT8Z1iXOfjSs/sUJtRKq/iqOU/H1Kla4utNyfm/+FQ7LnT/zI8Z/+A4p8NDYl399i/G9YW+So073TlsbmTlm9F2X8lxufLXZ/tS3AqXfektSNL39baO4VdCTqqxysrq8er1szXV9R12/YCnrH6dL/fev5JQb0RZ8M7v6+6rvsE527XPJFpuTft+xNWhu6vfIN/XVldtUU696a+qzqXPZNgZXXxP6UW3ZOd6LA4wV6DNxP0m26l6UCCTQmuJ1hRu7zybko31z5e2Z6vO3a2sq4/OY9O/Fj58sPOtv3IM23Lz7EtO8N2wbNrt9pZtHZerZ5Ru9DZtDX/TfalmjV6PuwH/2XVf5z9eOVX01z7yew3K384+0zFd9P9X8l+vLom0SWtG9XL5j6o3D/3QVVfgU/VH6TvN6Q04VL1SnFffq5u1S8WZ95W/WyBW9Wq1Acn0rP/Kb1fm+4fSfCPsp+uSmVXpXZWbkvpf1acpZu/25H9zapPWZ0J36r/OftS1XBe9l8IfErO311e2k+1n0jwA+n6Xz/kvOE/Dn32dTtvWM8aPvPgWcOcMZw//2riF9qH30z3d1MdFakPd/2Z4PL57xeXBfvlctEv1UuKfql+auF+qaoP/fKU9ct/ld7/3Nx3S/rl+bI+2ZutyvvkDwscPnSMfG+4fP77xSXL3t+W98PpBeiuNKzJ/tMEswl6cvigwBPI53uAvIwIP1kGx8pgZSlwTvX8edUfXd+flMD3W1+vwV2D3rmrOXD/pZTvD/x9TRqDVe3FmdlAounZBJUF5Gdm7zX4RvjnTr2dm3svzfeWyq/d/7rBygQ/bPA3AlxI8MsKyguz7P6v5+37EL78Ue8faHOvnxmufEUhn7MGtb+V0usp33Wdg6m8NGfl3y/msPKyRU+mZ3833X8h5f0PE1/+WvbjpIwH6Sz6RS56n9SmOS//sOgPGUvf/0wq7zds7WhI8KsFVG8roPadBCdTXesKkDcNr//EobY7waPp+ocLfqFzpOarBe/N155b6braeMStIq1Lc7J2JvGL38xW1Y7lfoxVyj9yHlI2jmrmUvpsyp++rT1ssInz0/PxUOfXf5p7h4p/nf1W9Tfzs8L/OMHXEryoZ2w/cLZ22bnaH3mm9gLnaefnaA/YGd6c4731Y57jffZj1PmbDz/He/4M74Hs3/oZ4rFuo/OLRutv/Hniov2eX591HPLrL1nef8PjoPIX8rPT45npaz/uOfB/2ncVv6598P6dir+ap6Hu7377oeepP+ws9S9lz33Ms9SXVf3D7LNVY9mnq65m3TWz2TfDWerrys5S/+kFzlJv/bCz1KvfyjZV38umiriTD56svl7xs5X3kix/KJW3OKuvPF/gVtWQtea4rc9+IeHWXPXNB895r7qcfavyxWyq8lZxznvlqYTDs9nfSrAofT9YdSHVP53S+qRvDBbnvFc1Z2NV95IOcy0brLyb0r3ZvpRvY/XKbDaVu6lqIpV3L/Hpj4fPX89pNZh9SmmV8PlmwGddGT4/vQA+rR+Kz4GsYYE+K/DQc6F+Imte6Pz72t/PvpXqWVY9kvC4kugd++z3Hjz/PvXXGP1Vsyz7kdRfv5b31w9kozWNRX8lXH70Y+JSn3BZZrj89YTLNwMuP1WGywPjJ+HytwyXLxguP2O4fDaN4YaH9MsoOOT9cjC35/r/AH4s+1ZVXbYs75crSUeO/TK1wDgJfVL129mPpD75tbxPEj2qLqYxMpro8aXsR8tw+fSCuPyL9PxOgsHs09WLsk8nXL4ZcPlsGS4PjpHm9K7A5QuGy88YLp/V9mt95e3VNqb0F7SvE79IctcH46Xw/q8luJauE7f8oDuVU5fT0kDLjFB1cO7/yusA9s3dzOsDBtP3WjdwNeVXPIAr2c/lOAFKqwAP1Kc0i3CnDLYX9ASUrhGUrhGSnt9RezONswBpPA9VX8r/pzWVxuZo7dXsWzn8tqVXs2/m8NuWfsT71M7P5DQvhc+k8at89KdSf2n6nP9DIv+PxE7+GZH6o49/Ruhzy3PJ8qSV4LsbE6xI7y8m2Jag5+PkUT5bWmd26WFrxMOel68x5fdpnH1Gx1oZfKbqg9Tm38v+avWn8vTTVf80+3drFmU/UXU4QU/2SzVZWl+2Zmt0P2bN+9lXqtcnOikMFfDYLydcvpX9ivyP2d+o68++rv9nqPti9tWa/zrdb07P//usatHxrEr/05DD/5b9/dpPZn//0dNZ1SfOZFV1S7KqR55NaV1KKxL0Z1WP/u2U//PZ1/U/DrXt2RfT+v0rsi6V+4nsmP7Toe7xbGvtb2a/Uvfz6dmO9M2vpO//JPty3e9mX5aBVN8X0rMfyrYsSnjWTaVv16R838y+rP99qPvH+T8fvvzIv8y+/Oj+hOef5p8SP//R/5RQulQdyTZXfpDG98Xsqaq/mzVXv53m3R9mn6uuTPDJ7HN6XfNr2Y/XjKbrewnS86rfL6D6yexzlSvuf67qDzKHwaQP/9+pDIV3073CS9lPVv4P2ecqfueD/6jqXOJ3fzM90/KuJZ53J/srCdf6qm9kf7n6eFGvlpnn1f8h/LPsheqW7Eu1P5r6WP+F8NnEz3+8+BfCA/9B+P10/xMf8h+E6QX+g9Ca9abyF/wHQu3fSfl+LXuh9t3sS/JHWfeiH8k+L59J3/8v2Rfq7mWff6wm+8pjq9Kzx7Ov1n0m+4o8avBL2Vdr/yT7gnwqXRvULU/3YrAyfZNg0UQ28OjfS2W9kH1l0fJsoO69rDfV9bNSna4/nep+LKX/TtYtX84G9H31Jf3nQtk/Ikr/D/GLRpNfLPs3xM8nWowAiSY/n+gxmIP+G+KXSmjyiw/5L0SWaJJ9RP0P9snVdL/wvym+mMPD+uQh/6WoeOKDb1S9/cE3Fn090e6/SGvZ8P3G6oNzX636g/tHqvfMfSXpkr+SP9NrBR1bv5vVp76s12sFHctpDH0u9eHn8usEdozf4wbz11LEh6vN/Yr5XM8Z6P5DYuBfsWfHLcZRYx41TveEfatx+IctHvG4eIy6xpLut7zbLYZx2OIjNRbysr3T2M9tVta0xTjuKMqoSlDZVMQ5Vjyd4H3bg7jLcLP9hnm8tcY/Xi1wUH+S7vfL61M8NFb9iKUab70kvdd9Drttf982K+c7hv8++07jJkeNBjNWr8aYagyqxgYP2HuN3TwlHqNJPLbuC0n4V3CmNvsSpu1e41FfKvY/5vXqfk3d46ixrhozqnGiZ+3dlPWL7oVfbjhsMFoeMD/6ZsN72p5r7OgNa9+g1b9dPNZ+xuii7Wgt2lLZYbG+lwzHWfG468v2/Iz4PoRdNga6jL5bxONqd1kfbDF6zYjvPd0rvreW/t9pbdVv60Nbjtj1SKD9AaMz+4BOiccEE6s9bP2j53fruGm0PtG43GbxOF0tb6O1YaV43PKAtbvJyhk3ui6xPOxJInZYadxr9Wjc75iVO2blUMaQXRMHT9ouvrdov3hs9xq71nJXGe6dVq/tP8rb2m9pj+G81uqfsDKaLO9K8f1Ia62sAfH4c2KP2WfUbPToNPoOWDl9dj1k+BPbPiYev86+qg1G5x6rkzjrwVDeesvXYP1PPHSjlTcY+gVeMW3lE0vfZX3RZPRoDjTpMPw3Gj27xWNQegI0iO8JIz6/VTxmW9+1hDZ2WJ4Wq5cYF32+2fqwz/Jss/qVXostX7u96zbc28XjaTaE/u20fCNluGte5QmjAY8Re8deCI311zGgc3GVlUnsOjE7SyzdYrj3G+0bxfdksPe11WBDoPvGcA3t2kMb4TPsw1pruPRZHYr7U5YXXBoN7zZL1xjduwN0ie8rabV6GMct4nH7Y0aX4YBvi7V1mX3TZddrLSWun7kwaffwFuge9+v1GR4Nlrc+0LvF8ITPNBoe9Qar7V295dX3S+2+y66J6++yfn3B6MMeDfbNtIrvsWGPBLxtg10zLnneKD7/Fa9pSwfDdwPWtuesP0bsfbvV22U0aTEa2toyv2+hRXzfCGNhneVjXwj7pFpDfZr/GfFx2G39MSnOg/rE945wzT5r9heuE9+DzTxaI76/E17H/gt4R6uV2STOA9jTA+0bDMasrM0BN/hZt7WJ/SEdhle3eAzd2lBOn9GYeuFDHSE/84C5NmJ1Mc5WGv6d1tbl4mvWgPi+K62TdW69lM6z7lBGjzivpj59NmX0XWd9oGm9OK9hDMFXGX/siVsrpetDnPPrpXTutxsNGq0v4rhi7m0wHNYa/ZiDPGc/9Gr7TvO8ENrZFsqsN1qzH6jHyuoL/W1xVHnboT1rxpgB/ThtdNto7dohPq42hHLYs6R5VhnerCnrDa9l4uvcRvG9VmstH3jSnuZQB/uFWg0fxgX7s5rFeSJ91x7KhqezTgyJy0XsSaKuQbvnW/ZYs7+xT3xswGvY57TI2gGfYA3pEF+fGi2lTeuNXowh8GX+H7XvVd5Cd9ljz1TOZU+n7YfL27lDfL+eytOnrcwthgeyN3UP2zXzAZmr1fBCvllb4KBn2Oi/UXIcpqzN3eL7MBVv1R1UnlJd5UVxmWFE/H84yN6M60H7RoH/6AwWOpXqV5o/v1baqiz+usyvl7ne1WftVT3pPTv/Y8beobPo2FO9adxw1rpuBJoMie/7HjL8p4z+o1Y+/O8V65+pIp4yf7dZXLZnL6fWM2Z1tYvvbx0Q3/c/bHmQmRrCuxlxnWhSfD/+mJU1YPTrtHybxPckIu8wjkelVB5otDLRAxaLrwXa5/H8C2TVJeJ7QJl/yLKN4Tv+ebRFXBe6Y+ea1No4qiziKnM63za6o+/reE86YeWb4nsYdxodlRYHDGf03t32bJc472KvsNJG90u2Gl1UP9exruP+RWv/JsNfy1KdUcf+ZaPvduuLUauHvfT8r4kzVJYbDiOGZ5/1H3LuqLhuOCu+T3PccGZ/sP23KceV+ag46Hx41q7ftnoOW37FY799p+PhtH17wOqeEt+Xf8vesx/1qvWt4qJjepu1T3FCJoLXN1o74c07Qz+MWT/uFR9Hk5Zvh7gMw57zLnFZsdHac0ecD8PH9J49rqvtmrml/dsuLi8qnLX7EaMdZ04oHs3WfxuNjr3WBtZLeCx7YtdbPs7YaLX3yOt27kLehhmjs+Lwivj6hxyo5T1nfcZ/v3T8vWRtO2NlToifobHf+geZ8w3D9aL4eqz1sLf9muXn/I/d1tdbpdQegz6Gzs++4eEy+sCnkAvpF/aZK69CFhq2/kG+ht/0GO2wFYwa7oq3jrfz1kec66Vj6biUyiXwxxmjN/Nvk/g/0vaK8/JJcX5q/LgSu0aP9c94cX5VXj+2xPNFfyiPym2Qdy2P0v2OwZuWvmrXavs6Vfd2zrPuFOdc5WuWnfudf69r4gl7fq7o1/zcryesv7RfZq3tb9nza9beN8THqrZbx8+LYXzMGk3OWl9of16wb49a38d1x2yYOa2sHzWOPh/XSsPXixj8HKcNRf25jfNp6/vbBY0qR4rY+7zs18R57mARVz9/HhVnwei7dTZm9ljZOl76Cpz1LKx8rHaKr1PIouhwnDmj1+iQjElkER1DzeLyTYe9U9l5jdXdI27bQSflfADWK71fIqUyL7r5MqtzlZTK1ivF5bd4jkSf1f0dww/bi14fsnvt45eL67z/sTXEM4aQwy8Y3ZHtOD8D/tFuwHweNBw3Wt/ru7TeVywWl1eU5kvFdTtb8/OxoGumjiPOt1Eb9TtWto5PHYfKky4Z6Pt7htus1avz6UUr57ble93OcdC8esaZ2ljOh3KuWKq00rOJXrL7jqId+RlzOvbeEpfrdllZSsPd9l73pLTZ9StG56sGnOMxbPRTHDkTZ8JwUDnxjtFJ85609KzRXsu9bGX3WfkXDd/d1v5XrA0XrA03rd1a/hl7z/k8l4q8+Rkeaj/ea2UNGT2RM7bb83tG03tW96v2/rKBnQeZ46v1HDfa3ZbS/4Dqt7cC/pftOeda2pmX+Ri/GeCGvbvh5ed05Fwe3ised62Mi0aPa/auyeh40d6/Z/Ues29UPnzd8pwJcMPaetvwOmR16rtR8f+FnrVvL9j9ZevHKwFPxpzSWcfGZmvPYcP1RUtnjO7KX1vsu7OGn+axf6HOn1V02WiC7DUhfgbUact31WjBGLpn728EWl63ey1rb6DXCfExTR9o/lP27JI9O29tvmo4oeu8anjot/sMz2mj4TviZz8dt29u2vfaXp3/B40m+I4OW51brN4bRqvTVvcFy3vK8GQuabpVfAzy781XLS/j+rSVofS4Y3W9Yani2C1+npXxlhzfo1Z/s33HXJk0Wp4LuNy0ZyeN1oyjW9a+V+z5XsNN83aJj627Rp+jAc83DO+DRsdb9t0b4r5MTZHjmJOc/8p5KzrW99v7m4aj5lFeZufN5vda7qi1R7/RufS+1alj5oA949xRzddidLtoZR+z58wh/hfyruGPT1bbxfmsnFsG/e5am06K8yHgnJX3puFwy+rWtt2w9+OWHjU6nhQ/q0zxG7a67li7D4n/L/iW1cOYOmG0P2H5zlp518TPhH1Z/Cy6YcPvlq1Vhyy/4qpzRfnAoOEzYrSHRvT/bst7Qfz8phcN38P27EXLi92T+XhBfO4cN1pouafsOx1b8F5o8Jbl329tfdFoytjScm5bvovhHWOn3+h62cpjbCqunI/OWDhjfaX3WwwvdJtdlqKHHDPaTdj1dwz3t8TP0rtrZSju74qPz6OWvmp1DVof7gm4aNlTdq90gQ++aP2qbbhp749YWXvFZYur4d0Ze37Snum3a6ws8ly355zpd1F8fJyx6zfsHfP1stExnqd8wPr0trXzDWsb4/eA4f2GOD9nLWAenDC6sT5Bj/PiMuZR8bMImd8nxc8m1HzKXw6Kn6f3nrVVrznjGN/qYSmdh68ZPVgz99q14sb68arR4rp9c8BwoR+Qh+6K8+ND9u0xw2WrpfAQ/P96HeNFjli6T/wcxtfE+Q52p+P2THUPHYs6XlXOUrmz1b4/GuhCHxNzcM7w3yi+xmGj0rHeazQ4avk1r86V7dZm/nmt5aD/HrbvT1m7N9k1Y4RzxlnnxsTPX9xnz1ibDloZewu5OD+L+X3jZ+/K/HnT+fO9hZ1u/sxn7f9bdq/j7x2ZPxM6t0e9ajhdLPo6153ftHfvhXKu2/174uvB3QDU85Ll1fl6x65fDfdX7Ns3Cpzzsl+y5/fE59Fte8ac0XfIZHfs/ry43gK/UFrZ+fb52qnzBr6OfV3HP+efcVbpHnG/OH4qzj7U77DDYcvFz8MZoZp2iPtj8Z8NifsC28N7/Dfo8RvFffuKi8qQm8oAXxM2T85ZxEeBn4qzI3sNN2zz/eJnD6Ijt1sb41lz2JWnxM/UxlfZL36mYTxHUueCjltsouPWDnwv6Pj42rDdTdqzsZCOi8cmYIectDo4C5DzD4lDwT+MTYG4Dq2bc+/2iccNDIrHAXQGOnaI+7TxR9Kvk6G91K+ATWvUaPiy9Q92iV5xOw4+W+xBG8THKb4bYl36xP0SY6FfOIMQP5i2R+0jq8V9UCPi8SLcE3OBT7IrtIf4Aex9PYbHtLiPr1H8XE7iNPC1E7tAHJjmPy6ulzNmpsL1DmvzJnE7+xart0f8HGPsIGOG+37xWChoPiM+VlrE5xpxT6PitqQpe98oPv632XN83lvFfUb4e/H3MD6nxMc3vtRo/+0IdfaIx1QRx4ePQWm41r7H1j1i6bi4HwjfPj55bLmd4jbWMfG5Q7zeqNU5I247ZuxGH26fuH9P6d4U+g7b77i4/Rgeo7g0i/uzNkppnAT+Tb4dtvZuET+/nzkMT9Gyl4rzGWiJHVHTdVYv/EzLZT50iMcFEVuGPXPcvmfOaz83Wko7tDzOr+0T9w3gT8EvTbvwNev1avFzbRlPen9IPOaFublZnG8TyzRS1s/wJ85enbR6WdvwXXPmLHESxP7hS5y0/tklfq7nVXHeyDqiuBEj2RXqmxTnQ5ruFudVrKXTds3ZukpT7KDwzTbx+ay4jIrHMmp7iAmGR7ZaecRLaBvVdqtrusoLnCmssoH6aLYZTFr5WtcKcZ2Ds9RNpsq/PW54aF7lLyfF4zOJVWq199BX3y83vLW+WfG5wZrB2JoU5zHT4nLEcvG1EX66IdTH+ccj4uOxy+pX3LDB7xSPFeoQj7sYsjwxLgv/cTzntcXyrhNf57vEz+kmPgGbsOZRGWtreE+7OLOauFl8pw3i59cSv7TfvtfrcfF4XMYS8SikG8TXhgGrZyI83yZun4cP9lo/4FfE/8caBp+cED/3HL8FfpGpsvKRVfAHTorLViOW7hKXocbtmnHfZn0GTyVOaVpcftomvqbwjwFiBTpCPdq2GA+5yfDaIu6DIaYWX/AOKV3jt1u/ztjzbvEz/4/ZO/gncZwzhoPKeQfF/S/x+R5xeZeYGnw80Bl5CJ/mTLjW9lqsxnwMOLG5u8V1JtZ5xt42ayfnpUNbaBH/j9AlHjMOED9J/BWxBFvFZfo2o88O8VjQNnHfJ/8RQD7ZZnX1Gp1izN6o0Rg5yuL8S9bsKEOw1rPODovPB+IsWPdZo4g5bBaXD2PZ6Mz40naKj138+fjyoQNx2Mgh6MXEBEHn9nC901Jkd/DbFMpjLk2JyyN91k/wRHz3yGNj4vsPJsT1g+3i8x6/NnJte6ib9YV1Z6PhwVoZ4+6Rm9vFedcSKY0f4ox4/sOzVVzWJ/4LHj0sPhYUl6NSqkeyhhF/RlwWsghyHuMSnyL4EgeP/DBreeDp0LBbPL4FOxG6rc4V4iSR++hb6qXtxBAQ48EZ4+Rtt34iNgk5hbVrvbjsR78gKzWLx+Wjt0ZdC/8y+je0QOdrFpfLiIHqDvXtCGVCf2JiW61da8R1POiDHDdkZSA7Ib/FWM5t4nHT6IHIv2YbqlC7yAXrj72W/4K4Tsv/CqaszFVWrq7ZxJgTLz9qeU8GHKkffsX8JV7X5nXu14VndFodM5YHOWaXtUFpgo1Nx9RN8X+XHLK8Kgvst2/QASbE/+GCfRGfyD4rF/sC/7oh1oB/fmE3Ud59RtyWct7agi0Zu5p+f1b8nPtLRitdO49Z3ax9t0L/cf4ocqteHxG3S7BnBb3zsNFum+U9aXjw7wyVYTfbt1tCHfr9ZWv3MaOnfk9M31ZxGQQZjj5CpxsRj9fabHmxPSm+R62MY6H/keMmxH3ao3attFD7xirxPWesVcSzEnuHLjAQ6myzvtT7HdY/iiPrwy57Pmw0mBTfO7BZPO7jnvVJg9GN+cReC3jGUSn9N8pO8ZjubntOGc3idiOlNXHdPEdePGjlz4rr6qyLyMlKL2LOWd+If2IfyQpx+RK9i/h64h9ZB1mTiItqEZctoK/yzJXi4w8dUtuzzr5ZKT7v4Xv85+OEzNvI872D98Vjc5GhLxiMWH7tgzcMzHeW/ysTf8NFe6cp/lfNp3qn2uqvG+1Pisci7Tcclf+9anRQGp63/ZenAs3vWuyR3kc/5xuGw02jIfsAmY/adzrvdNwcEY8nedny9on7sc5bilzZIs4/kCmRO1+1uNjtVj746Lvb1kevF7TP/8OIH/5Va9tr4vHR2vYbhtcu+/6y0Yz1fm/Rh3mdrIGHDF+dW/gszhu91K9wQHwPzkGjEXsl2+w9etoGu99m/aVlXbPn26yuWfF/Tr1qcdtj1m7+CaVwwuh0Tny/5jarf4u4n+xF8dg4LRO5ttdopNdNoT/w1Z61vDeNXvDHQ0Y3dH3Nq/NLx+Rmo1mrxcafNPruEfd7NBnth62sGwbIdZpvymh9s+jbPN1o+Vi70VHPWbmHxNeEVrvWudhs36s/402jjY6BY/Y9PJv9iFNWJrLOdSvzuuGtfaPz7ojM/2czL/OIeOwAehsxBdfsGy3voOExW/Rv3r5DVo6mxHCwx/qmlTlt/Wf0nR9n/UZr1tch6xNNO+z6iLhvpyfQ+Jz1oZa/ylL2P94zXPdbOfz/h7Gy3Wh32crrCH1w1dJucfkCvqjtuituZ9onLvO1Wf8OW917rU58H+w96BX3VzEfiC1j38uo5TkoPsfh+S9Zvglrw16Z/wdYRWfIO2bvdG1Bf39PPP5ksjjPOW/nafH4U+37Pi8TH2D+P+bni+/m4xcvG013F+XksaX6/7p14vESZ8Xj7W8ZHoqDzkvGFjx5xlLks7M2FvrEYzFsT/t8HNj+ovyK5+y9Prtd9GFFh5V/S3zvnvaf8oMz4nEqmv+OuN6JveC0eKyjjkPl4YftW2Jg8LkT/0I8k+Y/ZGW32zjQ706I2x4PWR23rc7j9u204YX8puPsdXG9eZ/R6aaVOWZlWoxk3o8HDacdRmvi7JT+OkaJFdP18Z6VsU3cD37B2jBm/bzL2jxpdDtjeYjTwN55V1w+Q//Tvtc5w37caGfEx4Nuq+Ox3p7hoyI2HT0M/lkfrrERrQrP2RuLjURT7OH9oS58b+iunfY86kboweiUyM2rxfffYh8CxwbDbUjcT8N/1KLu2Wt58Wf0hrz4nFrE9zDjg4o+M/b9YO9l3W6WUr8RvgP6gj1ker1CfP8iui42ab1GDm4KMFB2j258KOBBrDQyK3s18F3omNKxpvxKxy0x7PC2LpMlb1k8Av4VnXcHxPUZHbec3aD8bNqe63hlz9pOe6bzgj1O28Xlt/3i+y2PhXqmw3cHxP/jOW7pmZAi928K9e0LOJ21PDst5XyQAaOD6Q4V2Cp17L9veOKzwSZNTDl+MfybVkYlejk+Qvovxg2wN71P3KaKPmsySQUx6djT2GfSLu7rxKeNbYW9zN3ivl70PuyNG6z/sON0B2gRt3ESb8CeTWQJ9syPhHoGxG0pbWVlYltqEZ8v+AjRt+M7fCn6LPrZ9R6fC/5M3mu9Ol6xc5t/sRI/BPp1r3gswBZxvwY6HnZ5bAnYhLFN4rvVNs6GPIwP/Af4TfFzQk+lH34K7IIxLqHN8qwUjwtYH2jDNWdJ0B5sfPjsoF+vuF1vPOCD7VrxhB/gX8UWGn0RKyw/+6vZ5w0fZ9wydgbF5z91Ei/QGMqeFI8XYB88ct9Gcb+sfoN/AN68JZSj9GqS0vFOih1kneVTeq8R9zkO2PP+cI2tEztrlz2fFdf5R+0ZPuqN4jyWPmAvPTYFYjzYY7JW/N+qG63MTisP/sG6QqzISik9u6E91IGtfbX4P6ShET6IJvEx1yWl/81k7DLeOKMBOw37eBSeFo83wc7KOQzwQ2IDVhgMiOsHxBgxHtaFb+JaiM7CfGHdULyWi+9PhJfgL8FHwN4cYiP6rf+pj/HZLPPnCeXtbBD37bB/njGJr5c51Cd+Bg+6D/wRe2Bv6D/8BJxT0iB+tgDzmvFEvBPyELYB+Ah4sE8s2r6UBsh4zF/WL2JQtorLRsTP9Ir7WZmP6y3/WnH9m/mJztBg9MYfsUbcLwFOyIXM8U3isUmbrM0T4mdGDIvPh+7QJ9Pi4wmfIesp6xr29cHQjkHxdQ7ezJwjNgQa94v72J43+rWL+xoZ+5zV0BrqiXOANZT1CTmzT9z/Ae2pV+tgzWa+M0+JnWsV96XtzHGtzMsaC3gqLbFx9ojHpBCfRn/2issE+HDqQ8p8aQtlEA+wUtyeS0wc579AB2iPvAoPahbn/YxTcGgR5//EjcFrmB+MpSbxeQWPZd8k7YU/LZHSMYItF98+/5VuCrjWi/v9Y13MTepkXHMf7b3wKmS/deJzB16KH0V5zhrxWEnK5OwL9IjIk9FdGF/N4j4WTVVfVrlX5/xe8f+Qw/M0xXah4+mE4b3TcFC99Iq4fs4+7gPivjd0EtaaYXH9Zqt4HESrePwi6wQ6pV6vEo/ZIr4H/jBk35wR92nQ5o2Gf/R/suc78sbUtsrF4jETxH2MiPv7OdsLPxLxS/BU6MpcIhZE+3CXuI6u/VYvPm/WSan/kfgQeBLyAr5M/DwHxedwf3i+RXxMcs861CAuCyE3Q6vN4rG7xF4To9ERvkEGRqdk/zp6C7o2fhr4yRpxW9x5cR0eGwI6wlC4R47Gr8UYYF1gfOrzPeKyRH94x1hh3uHboUzk3H4rg3gBYopZv5UGe8XHFD4h/X5WnH/vlFL5u0H8TJ+4HrCObhbfq81cgVcTjwZPwwfFGoXvk1hN1g/ijYhjJe5J4aD4vCPeYpO4PNEvHueDPMtau038bBTNv1Tcr6nPdFwrn1ktfg4H46DN2kAsO3EDtnd9fjwS4wW+cZxi/2F/AHEY6HWsGXzLGqPlT4qfa4KMja+WmGtk8fFAV+Vryhf2i+uHE+IxKdvF49pIkR0bjCYxRm6P+Jq6Rtyv2xaAMY+ePWL5tI3YG7BprxQ/p409ANvF12PihpmHlMM87rdnh8Tn3rS4TwZ/7h4pjf8dKLtHtiO2CV8L85v52yHO75Fj4QXo3tjhj1m98C3qW2HfEMuDfIgOg74wKc4fWAvxz8XYVmKtsJkxJpBPicOmn5AD0T1oEzZX5hX2oKh/ItdjR1FabROPk8BuQJ8jk2yW0vOC1oqvNVE2wr6CDKDXF0Nd9Afy6EiAodDvzB19vkacx3eJ+8uIp0EPVnyJgxkPeGDDZa6Oi++joD/gi8xN5h6ydau4vWUi1M1ay9jqCnVwhhB6l+ZjvUIHRN5knxnxfKw98A9sOApN4RvaDi9BbkFfHxGPt8bWwtxoC+VjWyAWlTGKP3F9oBNxGfRbs+VrENcdsEMQV8latEf8LCvOesFejxzSbrRvEF/LWNeZR+zLwWYf5TzsEyvEeQjfjRm+Y6E81jRdJxvF107mjqYaC1Fv5VN/S7hnjDBH8GEQJwZ+7EPaIM7XlJ777Bl2U+RH9ITID5Bj4HNR/18f+gU5X/tW5xWxVNAT+RD6I/c0i58b1CouRzP/OkO9G8XjxLAZEd/SFQBfBzGE2EFajD7YgSOf1LJYX8aNFvBpYtnROdHTho3u0Am9eszasUU8Dgb7CfWutrzE9kyIx8IpTThvfJflU38xsfExhlnLfdP2YqrucEo8Bh5bW5RB2qyN8Aj4PTH6rEcbxWNesccS76NlaKzAEXGbGevO1tAH9DNx5dCQd61WDvnwBQwZzq+Jy38TlraGdCaUiV0U+9ew+B4B7Nesea1G/3XhHrw2ituYsLeMivMJzjnEx44e0SuuI+8z2hBfSLw4NuQRazdjl7hVeHir+Fne18XXEsZeZ6h3VNzXhZ6Nr6xfPOaV9RE5BZ2jRXzexPPAOQcNOY35Co/StMHqUVzPG+7qT07jtMLOVlBffsVO8T3uZ6T0PFAF9aedMBpyvsy7Uox5Hc8Xxc+sYo3Xb/ZYfvXv61iZtTarrq4+66NGI85J2VHQpkJxvie+n/2meDyW9t0hw+FVKY0bUnhZPDZf547GCKh/W+MF1HeGrQCfymWra4/hcrf4j3W+L3pQ/HytndbX7xR0y9uD/+yW+L541hh84cS79Ymf33FD/Jwazr/ZbeWoXvd6gM1FmsdUaNteNJxnrP2HLeUsCi2TPVlKJ85FvGVtuWJ0V/wuWZnrxWPx2LP/svj5U1cMl1t2r32g4+M7ob23rFz6Wus/ZfC6OG+cEI8HflE8rkZp8K74GQ13jXZ3Dddpy9Nqdd0QP8foaKCj1jVlNNExflP8bB0dk72Wou+/bLgSt6hy/lb7dkj8zCXO4tD3Ftc3/w+LM9aum3a/w9JX7N2rVode77S+Io5VafxSoO+b1n93xPUgpUeLfddg9NIxSEzUq9aOC+KxJTpXzltZmo4aPmfFz0vQeXHP+qHf0gHxs1mOFfTN/wM/aPGJrHeabhbf746cYbbz3DfCXgp0acWhSfwsIcV3pbUPeW2rPUNmZb3FJ4KPAV0eXqq46xxCvtRrbAGsnyfEdbXNVi570Q6IxwdiN90nLs+ydwW/Mn5D7HHsJSEOExsw68mIlMq32AqxjyHPb7ZnY+J2UHyl6PN9UuobQW7DjjQQ6iIOnzKGDPCzaDtVNl4ubvONciS0wIaAjzHiNCC+55b9vehD2Ll7DBdsZrQfHYo1GrkS+aMcH2RyfNfo2lFW4t0qcXsWOgl66TJ7pnYj/Kxaz/OBduhfyN/UQzwQshAQY3uQHx8Xl/9pMzJr+bfEmg+EslnXOwLd+o1mjdZe4i6iTkS5bYbLarvHbzAY6E4cw6C43hVxmxHXq5qlNCYDerOeYidaY4BegDyM/FZvtNRx94L1xwv2fHmARoNlhi82qS3iez2QLdG32JfEvm50Q8Zno7jNoSn0DXYF9h+Qsre3NdTTavi+IK5nElOD7IYcNxrqpk7iv4hHYP7im4D2+LpHAs7oAMCg+FhpFbcN6bXO7SWBxpqPcyHqDbSfVknpnq3V4nYSfNGK21Jx/gEfw2ZNbAd2XtoA/sgKjeK6EbFo6OXoaPDsIfGzqJl/2k/l+6QZe83iPKFHPK4GfsW8xQ4If6Gd+PmYk8w97LtDVj5xFPgTaSfxb8yBUaMt8TOMC2Lu+sVjHndbeezHGhLX//Fbw5vgndjdlB7YDdE/8NFE2x80xcbPGGU9bTS8SNFrNN8K8TmNvQPfbLQb0L9a7mrrk4YA4AuN0bkZQ0NGB+ybLeJxSrHN0IqUcdQk7l9bIn7O9RJ7Bw9bY89Iu0Kep8K3a+0a+0eD+B56Yjnw32I7aBU/5wC6o6M3hWt0Ovzu2CFa7D1tQDdkrWcOrAl44XeJvIY1Ar5E3Ai0WiLuD4njCl7TIn5+zirxeci4if5c+lHLZJyvKWt/g+GNHqt9t1jcrksKnZrE/1GySpwfNoWU+AfqGA3frwrPV4Xy4T3YdKEJvFnzbLZnzaGPsa3jB4CGMbaJNSv2gaYrA42QJ8CReULdzBPikuPZDE3i9uwB8bgwff5swPVZ8XVUr5dZXcQPY4Pm/IrV4jZl5smzBuvFfTs8XxHo0CZ+HhXxBeutTGK3pgI+G0KZreF7fHbry1JsQvQBMYcb5cExzljGbsi4g26Pidv4mVf1oQ+bAjDvV0jpfxHgH8QSwQvoc+YvOLFWMYeWi8ewMX+bwzXlse7wrjU82xi+XyPOV/tC26AtcTfEKyFLo3dhr8YGRswJ/GCVuO0fngbtN4qvxzqOnhe3C2PHw7aJHsG8BL+2cE27Vtj9cvFYEOL/h8T/NUe/tojL+J3i8XTN4vODvKwZPFth5RNPFf2Jmr9ePP69WdxmHOnbI85j9Pm10G+MYXQmve4Xj09aI64rIXNoHTF2GNqztjeHa/h+X8iLbIuOtUzcXwnfBD90y87wPX4azpmBdkvF7ayLpTSOgP0dxGMwN+EV0I51gL0RfB95PuM+ysxxbsIPOcuEPsF3h00ZeXWZuJ533fK/Ki6rqg2BPV9qv1F+NSa+T0Prsb2ZJbHPxKZEfyXy/KjlxeaAPY292ytCP2Af4SwNYnDYt4SOjd2I2Idh62P24Os1Z9DhC9dnGjdwWdwHzLkt+Ai57w3fXBKPg8CmgfxyWFyeVlvVLsuL7jAhHquP7BFjrreK2xS2GH3weXEeCD5UfDn4tGMMKOetoIfAg4lNgM/QH9gm8Lvie6IfsbvjI0TGxc5/KbSP+N4jUmpbifxoj/i5Juyd0PqJHxsz2mmZantTeyV27RlxXz0+4ElxfxR7kjvF/z8AvYivYYxQT3+4x38ybPgeFtfp1ltdxP8pzjvter3ledHKiXOePsXfjZ5OnAOyF3Ia53CCB2OMs4YuG05qt+RceOZ1q5TqGrSDdVLLfUpcbyfmFtkNf81y8fWPtRe7BvyfuNOUp3KnuL1M0xNW7qy4n5f5MCQeI0Y8EHv8e42mxLURG0R8mF4TR8N+zinxvTPYuhh38Ok+8dgg7EU6rtgXjL9vVvy/P8xLeFncI4LuPiCl9sxJcbsn8Qb4fTsNf/blcaYHsVb4LuG/A+K2NWL+p8XPc8C2OhrqbRG3l2y1b7U9xMl3ivv2OsTjDIg7Wiu+hw48iVdjbxTtZdwq/viLoAPnMjG3ZsXPi+wRj51kfA2Izxn8tPB3eM3LoWx4Hj5v9r5pPx0U94HSB9jwtC3MkUHxs8loJ+OJfmUMDIv7P9nHgQw2IR7zuklKxwM2lDHxfcRD4jHi2q5d4uc3xZjKeCYVvJL4N+hObAx2aeLjibfi/CHWPmymxL1GexW2XXjhrHisy2Doe+JgsP8zH8CNsTgmHheKTY4x3yEeh8Ac0DW/TTxegNhQ1tpW8XnJ2ULIgsgVxAcrPVaJ2xGIG4LfYSclbpZ4Xeb7uoAHsSDE5oADeODnwb9Ov2OLpFxikPAHxHUH+U7zz4jHZSC3YUfeYvQdMJz3SqkvhT1V2D3hW/C3KHcoHiuk9Jw71m/iBTWPzlv2im4QP/t5WDz2WvPreDkhPq52i++v1XxHA90UDoqfg4Zs0m/3x8XPdCYmQ+G01aH8rEX8vEVdp5mX8Db8cnvtHhsa/PaQ+HjoEB8fzEXsrzqOdoqPZfR5ZCNkYfoSnkW/D4jP+53ie2nh7d3hm5hOis8NZIFhcdkt8jF8e0NGZ3BiDOoaCF8FJ+z4rDvwR2zWxOfPWJ8MhjIHxGVOYlaIVeb9WvGY3CEp3XuHDNYtfj4TdnX4CTyO2At4aX8ohz5RPFRumg3AGgCuxBXhx2IORrzivkrmHftq8H0SA8m6wlzhG9rHWZGMaejC+IHv9ovvEYn78AH2tEZ/BHjQL8jy2LCIT2Jv4UYp5SvYkLGvIosis+MjizGozHf2VUCPGBcLD0aOZp3DVwRe+A4Yw8j96F2smcip0CHa+WKMPLY44sCI+YcPcx33HWFn2SBuk8OGhn8x+gDBh/0LrEn43fHD4Y+K/cJ6iY1Wy1wpvhaMie8zgi+0hBR+2ysuv8Hr0fviHlQd/zGOn/UZvx6+QGw1g6EM4pmJ/6Mu5gN2hug/wl7H+KQu+Bw+yVbx/ZnlvjpsXdEHAx8nRVYn5nZMnF/D1/BJxPmJbR7f8EDAdZfRC312Vkrn8YC4rAzfB+DDjFH08sbwjPUQ+wt6Tle4Zq1lnwjzc1B8L1+r+B5a4qkHxGMAsClitwIP+JryR87lYX1hfG0T5xP67JT42SfTRqOl4ufhsO9plbhuH+NMdYztE5erxg23XeJrPfbtveJ2K+xt6GIHrI07LR//w4w6A3rHrLitg7WS+Y+MhE+N+T0pvv63iI8X+otxhb0w7odgzUCWYKz3BrqwfkBbeGWv+Pm3+8V1syjrMuaRYZmbk1K6j4OxzHxknuBDZHxiR4WPItsw/6BJl5TuMcZvynzrD3kY/+gF4MB6Dn8dED9rFzkYXQFa4qtlLkBn5iB2T+jXG75BF+8Xj4/Cvs7awTzB3428jYyJbZU9OfrdCfH1ATy0nDQeK5aL2560Tei8CofCc3QoeBw2aMYkbdF8KqPtMVwmy2BbuMa3ORbukf+nLb0spT5LYkX2iJ/xq4Cex32M84Wm+PKmpFRPQ75BdoSvUB68gfgRaMx+At5j50KfhRcQB8zau0PcNoydGx3saOgj+C7fMpamxdcn5C9izZpD2djt8A9TN/6ELnGbDnOaWCj6nLGGfM/aDN8hfrAp4I3MOx2uN4V0RFwuxN6q9SwTnxuMV+UVxG2ylvSE96xXjKUoc7aHa5UxZ6U0fpE91ehukbaToT5oPSu+3rLO63fEUrFe6vhCb0AWYl0ZC+WyXhGLQnn4EbBPsbcOeZV4DNa86CvSsreL79mirmifZRyxPqyW0n/eoI/wHjs1+jb8jLWe2MVN4nYh9GlkDuiA3kP9E6Hu8j3D7ElpFJdf6NfyWDv8bvBV1nJwXh9ogi4a12riZPCXI38QJ0GcUmv4FntJTygr2sjpM+YUvmv0whj3gt9Wy1whPveR+alLcTkuvi+WuYmPPPov8e2yT1lT7FnYBulLZA7WXuhCTB5rCmOfPNgMAeYU9uom8b1Y4MV+KWJjsXshl7eGFF6E3xp5PvrM+wIgcyIfKf7HrGxiVcfFbRGKn85rlct0HmvsNftbo37Nfhr6Lq55rOHEgLJ+I1vHvR7EExGHRHzISvGYKnyik9b+tJ5W4JOEHvBvdDKgWVzO2SHO/5BfsLuSJ+rwR8Xj1uAT7LXWtiPjYWvcIS479Bgd4eebjWb14nqL4qC8dq2Uro3Rzsy+wk3hmdaHjY81Gn8xsQzEdBKvwlkg6y1PksErHxf31aC3aEy92uTxQcJjiRfAJobcfc7uiT1UGq0S38+qZewRjw9C92d8YtdpFJeFidtAjiAWHh6DX4o5iW0FORQZYkbcRsLaiN+a2ABiGxvt2TLxfXesreg8DaGf2I+ED0qvR8X5M+OCdYb/0WHHxH7P/Oi3fnxVnNfFPQLwcOzE8CnkdXQdfEkt4nsQd4ufBT5g/RPPjuMsCv2Oc0RaxeUAzlJAJl9vdNI2cLZGjN2KsciDAeBBjE14GHNuqfj8ACbFeflKKfUdsU6i7w6H+mk7eESZZLW91z2uzYGOm8RjHeDP+NjRKZlb+F/h253isok+57/Q3eL/bPmO+D9Sj4j/Q2CXeBz7JnFfGDQfLQPkDHgv632MscR2zDyL8gwxH+QnJqW837D1MEYZV/y/Lepx2PIHQ50j4T00ivtG+EcJdshGcf4ZeTi+T+IR2GfDWtsR6laerHOgPsCg+BzFLsZ5FTwDWK+x/XHuBXuD0X/gjeTBrx3PQGHOYvckxgi9Ghtc5HUbwzXfxnt4ODoQdl9srejD2NyUzlvs/X7xuYiOFfsKGy0yHrEULeLrH/pXUxnO2ICRP5FVkXGwx/SEOrERw5fhH1FfiWsFOkeM10K3bROPNeG8Sewxk4bHrPh6h10jnh+APZs5FPkqvBU/Gvajdin1/6OLDYXn6F3R51++X4O2DovvL2d8EguB/IStoU18ry79HfX3CMio1I0ehyyGvke/0Hb49rThzz44YienxO00+FmGpFRuhYbocsh0a8XjsGN8HHyW8UgcCDImMTvol8i+jGfoo3gcE5eLsekTQ4E96zUplaGxa6L77hPfJz4Qyjkozk/oR3gFce1a18vitogWe6Zxkypj7xQ/Y++Y4Un8LHvudK7tF+ezjaGsvcX3+d57bGOaj32CzLlh8bPHpsP/I4w/5P9Yfsb2Sqf6KxIdK1YW1/ma1Wbfv270uFTQo6JSCpkFvqLPX0zP1xZp3l+v2HfsU95v/an3Gvc3Y/ii8yo9zts5vLpO6p7TQ1avyg23rQzNe6CoNz8z+aTRXmlzXfy/G/CXl8RtSm1WNv8a2m15mwy3o1bnVavjZWvnqHiMkb67Kx7Tsdfea5lqp9O5omP9lPgaho1c73eI83JipZjXOvZbw3v0KeZzlAeIdyfegHWbtRh7Nms5OqfCanH9k/mgZdSL2wr7xP0KlNkhHheNfRjdHL8dc1nfLRU/Vw6bIb5fyo02LXgmccbwrXFxvxR1E1PCvgnsuk32boP4nNbxs0xchmReYotE/2wP9baGsjl3Dn8UayH4tovHjis8J+7fiXwe+R/ZCxs5MT/IePQR5XYZHekD1lf4QaOU+jxXiPuf9NlycT8w8YTwB9Z3LWOl+D694ZCHWHzsNcS+rxT/D09TuCcmIcowxC8Rbw+vxiem36BXxBge9p6yjvWL77PBptRj3yI/o4ciExMzwhqILI+sxX4e9i7oPD4sHovCepRoUaHzXfndG+l6n+Fo/Hj+vIIZ+5YzAt42+ly2/7ooj7wtLp/BNxR0zCpPO2/3ir/yHd0/r7xN+ZbqFGoPftneKSiPPWRlHxX/l+RGw/eUPbtl9WI74MxLxjb6ELEpxL4jJ2B/2Sml8nq/uO6Brgr/QE5E3yVuA194n3jsKLFJWibx4sSf8w81/G3Ui39m2tLV4vOZ+NxGmY8brThiNNH+esvK035S/U3XnX3i/7XRvrhn7y/bNzuKPBWLrOy3DV6yvnjb+uO8tU+/UZl6j+U5Yan26XXL/4q18V4or8vwUzrcN9B7lUFmrA063prtO8Vf58V+62fW3tP27rSVoelhS1kztX23rd4L1gfadl3vXgvlXTO6n7Y82jadny+L/9PpkuWn7tft/WUrQ2l7J9y/ZdfX7Pkdu6YdL9s39r/A/Bl9dcqebxL/P8hLxTf5v72g1zlrl56FccPwOhdS4PVwfdvKvhneIW9gP7htNL9s/X3I8irO79oz6n7RnqvMMWp9cdba8Jrl0/retP48Y3W/bji/bHTRtiOr3LQ2XLD7K+J8gHaetDIvisspxN9dsTFwUfyfSTfs+pLhoPTcYXUOWx9ssGevWJn7xW0c/L+GecZ5OlpXg+U/J342zgHLr/ed1s5doX+uWFnI21Pi9iGdo8TIHBb/BwW+Tb5hTxX6KbZl9j1tt3ycP8T6gvyAH55vtP+xTcEjWXexjSvPifoWNutoQ8J2iB6h+ZaL23H1nn0n2Fjx6cSYEOw67P/CboutB5mI+IIhcRkFfb7X2kT8e6u4HWCd+D4u7Kb6jj3V7APELoefh/W8RXz/KzrXxlAH+9CQedaVPceXNRjq6Cmrs0ncz4yfa9Cerw440XZ8e3xDP3DdEu6xS7aIx58iSyGro29Gfze2E4AxS7kNofx4jZwb5ZOVC7SbODhk9kZxWxfjAj/s2kBXfKjNoQ7GVMS3XtwXgF0Af0JDqD+mjVK6507H6DJ7tlQ8BpNxTL31lhJTwH7KqKfgc2A+1ouPJ2z1+Ayw1yN7IxOzh5D+QH7uDMA+GmxFnDnYLj4H9orbhmLMDP3HGGfMEV/DWMHOvK6sz2lfjDEEL+5XiI/j7tBv2Diw50Sfrt5Piv9feSDUxz5xveaMcuiTeHPFLWs3NkzlawfExzEAvdFdmd8xJqhTXOciVgNexbwhdl/f4cugjWOGI2ehRh8o/F77b4+4n39NuB4UtyXSTmx42DS0TPbtY4PDhoU9Djo3SamsjOzM3MNmRbmsLcQyEUdJPmLh6Dtsn6cDfrRhQPy82j77HjkZfomNGP0SO/gGcf6FD2+juC6rdH/H5NxZ+98lOizye4y3Z37Bd1nPmI/sFcLei32fOcLY7Vrg+YDhqd8gz5Ovrew7fEL4ydGV4cv94ufCTIrHmCpsE49hRMagTdjz9RlnPGOLxdbZajTFJ7dKXB/G1kueTvFzctl72Ceu+zeHdvRYvap36ZjENgidkB+irQS/wKD4+QPso0f/Y14tFbcRrDa8FVaI2wuw7cSYbtqBLklf4LtlDzjrBfb+RvEzENrFY06QXWgX71lbOsX/Bwo/WCG+ZjPesIFiV+dsjCiD8D8b5pr2vcqm+NX0e2RQ+5/nvC+B/a5T4rEUOu9uitu0VO/YbfioDD8jLj8fsTJVt9snPpc4C+eK4bpF3P62x2iwT5xnoN9y7hz+RM7OvyIeD4QMSizKEXtGTDi6Db45bHbENLGOst8KujEPDojr+MTO4bO7aOWfFPdDo0OpDXwyPMMXPiV+lnKP0YuxRNuZx8ipO8RthsRvxhiz7VK6V6TcX0JcFX76AfF4wWh/hY/EmCd8M9TDmoRfgf0DStu94vs+WPeJ5Yi+cOxb8Nep0DfY6ejPHvH/8dAW4qaJE2WNH5BS/91YaAfxxbvE9/xqPfAjzjsYEo+vxK+3Wdwm3mntY69gl7ifiLWetQF+RzzWoNEK3YL+gt9H+xQ8Epss/YBtsSm8mxD/hwUxXax9zeJyEWsY+3mwr+MrQ59gXnG2MXMVPkY8wZD4noS4P6oxXOO3xc/P/o1N4rGlMYYUHyZ+RsYzNj1og45I3C+6gf1rOscpxpOyjjI/N5YB9lXm9vaQTovzmyZxGy57mngW4zqRm9j/HGO8kafwPQ4FGq0XH/O7xePRB8T/7QFv2iUemzRqeBC/ENcz9EH0bHwNI+J7EtEL+JfNlLisG32iPeKyI/N7QvzsSvo1xiYxv9mbyXxYJ847kEXHra8nxWN5FId+8XUNu+2wePwC+hJ6I33ZZfRYKb7+YkMmvobzXPRaxyfr/UhZnd2hDuYvsgnjSL9bG8qJ+gPyKfoW+DO/8Bkgn2HvbQnlqlzQLB4Lt1l8XWB/Hfv06IuoA6IDMI/6xf1+zC98GY3i8qQCcQnrxPeyxfMxGGfwamiGTaJXfJ8YcQHYolrE912wd1Nx13lwSDyehDLiOkfM93Col7hEfJXIt8ityKzgi19Oy1pu11vE1wV8mPQJdSGjrxff4wNt4eHD4msr9ibmdbP1B3ufGsR1MXznU1J6xlG9la1yM/oRcjb5OKOD9QK9LtVZ8ar4ee7wTmSPFkvZ44wuOxuuiTtpDm1gvWe9w8eELh/LirBXPP572GiuzznDV0FttHsMpz3iujQxR5vF4zWaxfeNYKvg7ImBQFfmFOOg38rfHvI0i9tYONuXOCZsNm3hGn0O+ZNYZHSVzeJrHHow+0/1PXyUuvk/Ae/RdbE1HBFfe4kVGgzfsubT3k7xM5axyXGWAucmEUOPLtsfcCQ2BV2/Xfw/OcS8DYQUGZb1D/si7eMamvG8P1wjwyFPIO8gT8SxtCGUic+8MbwjX1e4H5RSnFiTiZcjdh9awisYz43ic6450Js6R8I9fYuNUum8XdwWORryxu9Yg4ihAU98/vR1Y6D7KvFxQ5yTtnVa3MfM/jqgJZTfFPphlfheUOZnef+gRxPnqu+QK7luC+XSvu5Q56pwvUE8boN2EidHHYwbeBBzBLkXuxo8C3tJxL+r7HpIXPaIfdYspWceaH/h/wdf5n93KI99pNih+kJ5rDncI4cSmx/jIvT7+tAe+nWD+F4vxmC/OD9EtoA/rw91g1ez+HhmLUPOwP6CXYu62X8GHuwpXRr6nPKxxbBOdYQ2LpX59bBS193dhste8f1GmnJ+PXsYiMdHzlE9Hf89/8XCLoEtfp/l1z5l3dsb6mKdAlpC2Zz3E88bIj4WvZDvuqyO7aHc/eL7foixpZ1a3pGyurHzInvVi+uinaFubeNa8TiDreHdoD0DD/p5QEr/N0Ss7/rwbYxnRi6gPk2RdXhG/slwzf7sDaFu9gEMltWBbMl4w6bTJKX4RJkKOWOTlNpnyLsp5GF/A+sG8dnqX0WOxd+ELwCZFjkTmVNpulucF7HvE33iOxZzqL569VErfybOBbuH+pB1nBwSP0sHmwznAVpaoec2HhU/h4GxAx/UfOr/1jFEDD7774zu+T9JtHz2DCNH4BO9ZXi9Lr7nBF0Rmwv/BB209jMnoq7bLG6HQJ+Kfhv6ZkXoI3y8yGsD4uMs+rGQ4bC7IN9r2U3iaxFrC35gTReL6y5DoQ7NT3wp/Bl+SRxPg/gcf0Ncb+0KdTeI252VXxMTi41ltdGi0Wi1V0r38sVYZNqr+RknyMOnQ78hw+s3ytvQFfgevYFYM+Y6vlPOwlpvebeJ64bLxfeXsS8LHZBYAS1PYymQPbG7EQ+FLQl7Gj4zdGh0F3ycA1Yv/I7viWFAlmOcRH0D+ZuYPeyAnAFGjEM8z4Q4bJ6jow+I+wAHDTh7bnegHbbCdeL70pnf+AqIZWZfR1N4PyjOV94Wl0cbQ72d4Rp7W5TPmFvomMSlEvON3RMfZPS7cj9h3+0x6JHSdtMPzLW3xG3IDUaLe+I2hP0F/SqeknlbvsZlz8f/7xTff4r9F90YHzD6PrIqdiR8A83iutkq8Tmh73U8nxO3FeA75RxqYvc1lon4zvviY9T8qhWJDhXYGrBFRzlrk9WHLIRdG70O2QC/C7aCOF4Zw9iKtJ3Kw+M5iOhaS8TnErpHi/i+2LXi43Y4PCNOYo88GFu8Rvy/C8jM66xMbMx7xOU2xgxnNUT7dIxt4MxhYrmRdZH7mkL9yKvRT6B0nhCPDWgQ57PUpeVvFbffU9ek+P8HI70aQv3N4d1K8XPcI/SGOuGL+LaVvpyJrLjeEN97Ah5LxM9qj+U2i8cYs77g10Omhi9qutTaGeXuBvE5s1Z87+0qcb/jRKAvc4BYrOX2Db5R5h6+RMWNeQmP6rE2oQfsFh/Hys8u2Hf14buod8TYNdq+1OrE1r/B6sA3vCR8p3i+IO5rjv7rDvH/dDBPWW+wL6G/00fwS2I7iPfAHx73ELIeIsMwz/DJ8j1xxcSv4BPuFLdrYivGx0GMgOKEXyu9r9A2qlz3jvVlg12n7yqUx54S52fYk3WdvGm4q2yLLYazS9iTf7j4riKVWfGsuM3qmLhN5IT4ubTDhvsO8bOIdf7vF18fsAWxR5F1FfsR8zT6/rTt7I/aJS57bRfnlaPhGrmuTUrtAFGmpS6VsYipgmcjxykQo4QNkLh3eDI6PTZ4eC559buV4jE98SwEZODVUho/gGyNXaNVfI1gT0f0WTaH8qkLO9qA+LpxR/xcY/qDs6WQM4hzwj7Kvxi7xG3B8axa/EKbrY+OiOuYk5YSs7TD6jooLktyLgNla7ozfMfZC2OGG3LhtLjewT6FHiuP/jgc+kjrZC1CjsTGj16nuB01PCfEzybC3gCvw6/IWS345dCzRsT9COjc68TPjKet0Q6AL29WfK3dae3nzLBucZ1Tx+Ahg0327WHxc6Xx/x8SjzXusXbNhv4mtgM5Ykh8/hK3C38YD98dFtfbiJOAh3J+0jbxszGR9YmXmQht2yeuD7A/f5v4mJoynGkH+hv1nxK3KcAb6WPOG+NMxxGjlY6PE+Ln7WIDQh/Fx4OtCP0KuYj/7mJzZU8bfg/0IM6so8+IJ0YPpZyoe2HPWi0+ftgbF2NT+Rbf1rj1/4z4/D8ovsYRH6J17RH/Vzp+rF2hPGxAUZ/GvtcjHm/Oc2K/kGfhL4NW5ph43ANnPKGLbrNv9ovbeFgbFO9l4mO0LZS3yZ7hz2edof9mxeNM2EtDPAs8elpKz5FAXt4qHlMyGHBhbiqtTojzP/bmYE9k3y02R/iRtuO05UEXRL/cZX1XL37e+ElxexLzStuyw8o6JG6DIb5xVtz2NCN+3iD9T8w75+9oey8aDnvEeRv9i2zYJ+5Lmg40x1/OmQD4VolNop4Ro0eX+Pq4XTzm8IDdD4r/TxCabrW2tgY6YKNR2jCG2LNKLF2XlYsdeFBc5+NMil7x85H5N32fPZu175kr2BzxyewSt8dgR0KOJKYEe3Kj+FrGXN8uPq+QazifEjtJc+jzY+KxM81G/82Go353VHxubha3JxLPgG8RORfevE3czk3/4SeZNZgMqfbDbvG9zKwbzFWgU9wXDT9mDs6Kr3PDlsY4umPicQLwJWSqbeLznb3R2AFWitukicHQ8YNNjjWsV3yeclYHOjU+dmRx5DLWYcYvbUePGbV2MB7wBzFGsMkhK9BeTRkLMS4DnztxbPDZ7tDH7eJxtsQrYJeKNhJ8a0NGh63WXvzW8BzOBu4Ql0+jLs+4VVroeNklbtti7BHT0SduRyDuiJgS9lrj42CveIO43ZZ5vE/c3zoi/i8CYsOiPwDZh39DsLYcFNcztC3YWbEdbLXn2Iq2iK/7+DKaxM+aQ49g3WwQP7e7S3zf735xG91+8b2cjAtkza5QB7yPGEr8a+hNmw0v/meOzQFddbvRlthb+gJ/couVu0Nc7ukSjxNlfLHXAJkBeqI/40vC9670IA6v12jeZ32CjRj5j7mNDbrf8GU9GBLf4zYqrp/B9zvE92xMW1ms8/Al5EDsotgUxq2OOJbgt8QCI/fE89+HxeMzmZvaZuJaWafZy4O+SNxStAmid2Iv3C6+Pihdj4mvj9PifgjmFDIOPDzqzNgchsR1XGKS8Ld1BRyJUUMOR+eJcX3s1RkVl6tGxG0UyNnIAPjxwGG/uB45JqU6fLRljxsdWa9mxW2r0Bf5Fdlbcdgpvk7wDFkReQu7apSf4V3w9Rnx/ZasYcTvx1gW4kk5n2WH+Jzk/D3sIDEmCPsWMSHYNfFvYXPgXBVtN3vg8AH1is914sUok3YQi0RfYE8aCP1HvBA+LXQn5cX8pw/+SXxqlAc565ay6XP+v4T8Ad9uDc/gveiPrO/sGxsK5RGHhN15WNw+gr8O2yD2DOyKbQGQ3eEnyPr4m+kP2oVOxVrJmo89An8WOg82POY56+sxcds58fDoiUOhDORU/FKrxX2MzGti4Fjz2sXPSqONzBv8r/CGNeIxuzrf2ROySdwWgA7XEerkf1dKU+w4WtcJcd/8eKivy/IPS6nND7sLsSuN4ue/wCs4PwEdZa/RgfjXvVIaZw4f2CwuZxOTRmwE6wq6JX2M7RK5UMvUtaoh0B1+gO6veV4UtxUOip/Bge0txtOiI7NnBvkam8ug9SF6MXFUxBhMisdRIWsyV9AHWeOi7Mr8gDb4SdaJ64LIWINSanclLhTgvZZ/0PDHj7zRyoCPQudu8fWNcyl5x5giP7o5vFHr2ifuW8JX2xpgh7hM/4J4/B3+lHJbBLZAZG9820el9KwrdAr8T8jD0IH5Av2Yq8SZQFO91vmFP5bYqRjvT58Qx8aaG3U19JUz9rwp1B3j95BxuMZujc7P3s8G8fFILAX+B+jQI+4vhib0M21mPwfx1MTyDIvH41FGXLP4Bh1gUDwOBRkPPo6NN8pd6IDs8dsurjMje+t4U14Q/83E+ovvininsYAHvJF5jW6Gfn5GXOZotvaozlsvbmunDQ3i9iJsljH2iPFOLP6A5VsV6MUezhjjTwyk7XWqXCyuJxH3xFqDjMe6E8fxSvEzwLCpR1lM8UOexjeotJkSj4FWWi6W0hioCNjFkfXhP5RPvBR6L3IFfAyfF3HY9BNjDHkl7lHDr0JfUifv4YX0Cz6F9pAf2xGxGMhE5O8K+bCJ4kfHV0f/sScNGwHlYAPGX47/c0hK5wyxAi3i/nb9bpm4v4dYXnzE+JzRf4hPgnfiI+sRPw8K/owPj7aNGM7In+yHaA4QaYnNpF1c5mMMQ2PmfGOoGx9Xs/gcXSnu42b8E7sDD1kpPs6JMcWPv1RK5xo4Don/f/kZcZ87tsmV4jIa8cPI4VrGC+J7kQbEY7yxP4Fnk7hfb2XoU+Jg1tvz1eL7vLHPInvRFvZco4euEfdtLiujD3y+0/qOfif2CfuKlrHOYIWUxjxwzXvlp7oWI/OpHIZshe0FuwY2K13jl4jbIRgX2BeHxOMIN1j+l8RlWfAeFNcB0b3hl7SLsbRLPDbjBXHewfrUGPCB/74mHlf1mpU3YzRZIu57gN6r7Hm9+LlwWuclcZ61077fKL5XatjyYuPHNkvsGTYj9nUOi59rTn3wpY7wPp49AX0YZ83iMsZS8TUVmyGyNj4E/JXsFyOGClnleFpznhbXj/F7YSsfklIbMOeB6zU6MvsE9JmOp0Nl3yjtNV5sIjxDZov3+FKJ18antD/k2y0P4hPv0Vti2dPh+UjAaVNZPvSl8ZAfewvtQ8+YXuDbGK+GXfZgGY5d4Zq4rQOh7eh+2JSGy8rFJjsg7iNCBgE//Gl6j7zLXiR0OHzd+AeJqSZOejp8zx6BqNdvDu+32Pf8E5vv8d1p+7FFA8ji+Ozx7eIPog8pJ+p/I6EcZIDxsvJnA44jZd/qs7fFfcybxc/iQ3eE/2Grpy0joS58pejujJGp8D2+xPg9vipkXvQF4nbQDVmzsE8oL8ZmAg3GrXz+s46NjHMHaTd6Cv96GAlAXyuu6HLETLaIx4jsM7pq7BtzfJv4fg3mGnsR8AfwLyrGJTQAP3CYELc79oj7y6MtE9qii0S6Qvsj4vZFeMis0XxcPI4CfjEa7pmDzeEanPaJy3O0Ez6h7d0jbmPGD8dcZr6i7/w/7L0NfFzVeSZ+7nyI8Qe2MRhwjPEHOMZRjBFGUSeKKksoqjJRhDpRtIpQhtnJRJ2ICqEqiv6qohWKoqquqghHiasY1XEcx3Ud6rgjhzUGDNShlKX8CEsopYQFQij/NJRSSlNKCOzzPvccZkw2G8+k6y7dV/f33HPnfpzv837fK3ecP2ccbXBxVM7ukb++8vN08zcWOZlOOBuMy9/dd9CiOpKjddI2ZwuVfOciubg119dOb5jIK8fJPy7u3Plg03lj6eiRWzNOxnA+QeQdGLDnHV2bieT8aulILs5dyp+O5HiO89+52BUnuzna5urh3qnsiuTkVKc77Mirk/s/7nsiOX+Bi69xGMnrf1ee8xO4dd6XN2YtNk8XW+Teb3L+fDfvd0RyNM7931jnf3dzcC6S+/60o9numelIziY+EcnxL+dndrFg7txg3j2O3jk/y/a89vZHcvTI2d0dzXdj6tbzbCS3Rt3YpyMnry0XR9YZyf1f9CE7xm6uuvFw66LHluH8Nvn8zf0/B8crnA3CtbU+knu3rzGSk+2drcDpIfm6tHs//YFILp7E9bezZXfl9an0vXvnz9nBpyO5/4Pu4iuqI7m4+7pIzv7i/Cb59u9Y3m+nfx6J5OJmnC9Ezu+O5OJynovk9Il6/1nPRHJzxdlEnT7v9MKYHRvnR3fxXfkxvY7/OF3XxaA7uTT/XV8Xo+S+s5yvQ8qzt0RO5sfOl+7iTl1Mh9P/nU3VySnOLuZsDasjOf+VW2P9Nj8Xayr3uXfs3Nx0sT1uHTnbkqA0kvvfR84G3Gh/O/9SfyQXn+7s6w9Gcu+J9EZyMS3OBufo+45I7v1ZmZMuXsL1sRtfx9Mlj6lIzlbnbJIuls/FSzjfiosBqI7k3vlwPjlJn7PHk5GTv5Xi+ICzA9bb/huK5OaKs+87u3dvJPdNL1cfF0vlYs2rIjkZYHck936T45mO9zn6LX3idCtnJ3HxuHIcjeT8VFW2fCertkRy9u2DNt98W4KzSzldx8V/N0Vy3zVx97l3GZ3NTFIXJ3Wz/d0Wyfnrmt9y7K65d6bvj+TkKqffbY7k3l12MUsuti9u27o1krMRb43k4tsSkdy3D9z4brFj4cpw9qt8G7X7Lf2wIe+3+75NIg/ufaLySO67mW6Nu7j6ikjOtufsse49hXy7RUNevu5bIi7+yen5zjbg4iBdG1ysVF0k9/009x6Dk3vdu3Xu/4u7d/cdbe7JO26I5Hwnzt7i7GZOT3J5SB+URnK2akcrE3n5OduXo9nO3jZo2+/8VdV5zzs7qLMROru183k6H4eT/Z0vLxPJ2V4d7XE0yuXTFcnZjXsiufXiys6Po3D2m4688txvRzv2RXLfpXLyZVUk9z+Z8u1+rg/zfVkuFtHZxNx8cfVxdXM2JReHkMrL38UEOH7ifB75NkFHa2ciOTunk7vc3HbviDtbd4e97tZ4/re8pI2bIjn9rD6S+xZFJpJ7h8rVoT6S881XRE6WoZ3NL55XTiySo0ltkVwcqbPHOdtRZSS3ft07UVWR3HsR7l2cOps6mcddq8/rcwf3HpSzrzuesyWSs8W5WBHprxHbXtce14/OX781kovrdM/n2/7qIzk5y9FHZyt3coyzhzke6HjTbCTH21y8sPSvk+mdbdjZElsiOfnUjbGLg3byWmsk9+6RW5/lkZxvKv97ga6MY5Gc7cK9L9EbycXnunjj0bx+cjYgF9/sdGUn5zt93t3ndGcnPwuvcX4VFwPtbDbuPVUHKXcukpObXfyAk4udHOJiV138lJPJknl558ftu5g+J9+7OAqn/+bL5q7uLmbF6WBOL3L2KTm/O5KzgTmbTldeHZy+72TC8bc8m4zkvlHp5DsXE+lssu6dZacPuLFwumYqkosJcTY1Z+tz/ePkYmfvdrq1s7cM2XycPcnpm65OLv7N+VddnH6tvcfRLRc/KPc7f+hkXnlyj6zZnXlj0pU3hqP299ZIbo4632VjJBeH7XyazrfseKPjGy4+xL1r5vRcF4fgZFHHo6Xdzibg6pQfo707kounk9+OTsjaK43k/Cutto61tr6Oh7oYKzcGbg042c3J5E6OcXqDkx87Iyd/p9ylnW85J3Vw/7+nNpKLCXBtdXKHk98cP3f0xMnH7n9eOdpXFsnZ6K1+9qad0PEF52NzvNL5kKpt251dbiYvX6drOfncxaBVv+UeV2cn27pjF3Pmzsfecj4aOfn/8zj/oPTD5khODnTvXMfy8nLxqK6fE5GcT8SV4drreIKLbXZ+IueLd3AyVFve/fkxYc534mLV8t/Jc7zsvEhOdnD1cLERrsz8fnP55/O06jw0vuU+x4e6Ijnd2Pma87/t6SBjuzWS49uCm99yXzqvHCfvx/Py64vk4uDyn3Px4i1597o+TOYhv65uHmzIu+7iUF1+zgb7uq2rjL3MS7HnyHdQXMya1Fv8QkKv+iM5O4CT6SYjuTg3FzM6F8nxAKcjunUl6/4W+3tXJEeX3XrJRHKxRu5/8TgZ+XgkF+/t/IfOnu70ecn/QCSnA9t4Pn7TIB3Jva/i3lGW/n4okqO7LiZH6vpKJGc3dO8NCS0TWrgxktP3HW+KR3LvFTZEcvzY0d+d9jhfR3PxR05OcrZYJx+7dx4OR3K8el8kF/fg4tIrIzm+LHypPnLyt07l3i023RrJ8XUXQ+jml5PBnT/B2QlcDLPjJ04+cd/cdrGb7vtXkv+DkZxv5EQkZxPPj1FqsNcfieT4z4ZILt7OxS+N2npvzrvm9C73/rOb467Pnd3C+frGIif7Kp0t1ck5zqbj/geis7H2RnL6puO17rzzA+X73V37ZA3NRXIxDu470O45519zMobTNZwsl/8NlKFILrbJ6Zfu2kDesYuJcvYat66cb9DZx3sjufdLXXudXdB928bVNZmXryvj8Fvyz+8DJ0vl+76czJUvWzo/iOsvxy8zeXD2Quc3dXqCi+d268W119nDHX1z88H5WWRMXJyE08mdPtUQycVUu/cx05GcLiN1bI+crIMdzut/96xrs3vO8TJHl1vzno9FcvaB5sjJepOjL+684zUuFsbxSveekdOP3Fquj+TkX0cP3bsZbpxd/G5j3rMubsHFvTie4c45/6TjUY5mOh6dyCsz3x7k0JZXnotrabBw9XTvk7tYkny908WpOB+GW6fu2/VO/irLy9fZHV0MnrOxTUdysfv5ZTTm5ev8ne63k+UqIzn5yI2zi/WJ5d3vZOzRSI5/OjtD/vcGnL3NzUUn01ZFcrp3d14Zrp0ufkbg+JvjPc7e4ew2zg7rvjviYlgbIrlvHlVFcvHWPZHcfHI+FxfP5uQ4960KF6vs3gtw9ivHh+vzynJ0w8V+OZnX8ZvOSO5daxmroUjO5jEQyf1/Kqfnu9hAZ290MofjH07Gb4jk7J4uVs3Z7OW800ncs87e4GRmFz+UP2fqIifbItzadGte8tkYydmi3Vg4ec/NNycTOxups8m4dTESydmHopFcjLuLLXI+BWf3qIrk9K7SSE4+jUVO/m5QVSQX2+RkBRej3B7J6VOSluWNi7OLpyInf4NnMq9eTu51toVYJGc7drTS0Y3Kt8DZkpxN1Mm3+TGCzrfgfIL59N+N44ZIbn7mx/w7OuXsTc722RbJxe07faw1D/nna/PyczZaFyftxrU1kpPlmvPKc/PLnXd2UTd3nZ3L2fXy4wdjeePsYuEkdfPG9adb107uq4/k/hddvl8m/1lnv3VzMZo3Di5u28WyuTXhaJjzUTpe5XxJTl9wdKQ2L3/nd8yvi4Pcny9ru7Y5/7TjJY4mOn4TjeRimt1ajeaV4Wy4DXllOVnb9ZPTsdx1R1ucTlIayc27aN59bm25eerOt0Ry7yO6ueH4U74vyfWPm1NCJyciuf95uzGvH8oiJ/PW/Pg0N1/LIjne6Oyu+e+EJCM537Vb45WR3HtBTi9wY+tkkda8fJyeKef7z3jN1xVNh4mbBtNsWk3UNOJXp+k3XThqNy0mgfPtJoN9CldqTQzXGk0dznWYNlxvxCb3dgNDQALXqpBTHfJK8P56cxHPN5pq3p3ikZQnV+pxXMU7m00adanAc02oT7VJ4tlqnG9Fuhn3xfBkG8qK4dlq3NvmvY6zzdyGgBiPGlB7/1wj0YDcoqhN85tbzF77eVsdntjEo63cNwFtQA9a224qWR/JoRZnW4EmU8an4mhFA9oieVfaGrSipnVsRy9qkcY56cNOtnAj27scZW1CupH9fBHaWIk+qMfvLXgyiadqzUrcXYHrG3FcbcuWnOp5LL3TzhLj6J0OpMO4eyNQhdpJX0mPSB2rWMtm5BnDlUpcS2Ivo1XLVrYiH3miHucqcS7OsYzhWj3HXNpazZZV43ojcyrF0UbcKff74ypjXIU6SQnSdn821HFMy4BqjnkD2tfG31WoXxmOGzHLKplbmSlH7eV4q5kyt2DG9ZsBM4pRbgee9ZaYFzxjnvMMtiXeeeZJ8zS2R5A+BjzoRbwAr+GqecIL4R7jlZiXcO0VPv0k8ByOcAeee9k8i+eew9GLeNbIPXINdz9nXjO1OHoMZ35snsAdL5jXzePMeSGffQxnf2geNI/j3hfNa8zxdZy/D+W9Zp7HUw+grJdxxw9Rv4eQ9wv49UNzL9KXpBW4/wX/PqRSzn147knk+gjOPI6cXjGP4vc95hngZRw9ifseMSdw9QngFZy/z9zPdj9gHkY5T+Kq1OoJnD9mjuPccZR2HDmnA0+gpAdRi0e85bjvWbu9jO0RcxilPYlnXkaOr+Cp+1H2PpQp29N47l5vjXcO+vo4nn8IV1/A2VtQ5n3mCO54HWceQd4v4Okf4soTyEFq5Nd3P2v8BO6XHt2PMw8gj2dw5n5uD6CGz5tZnvdHcg/qWIJnpLcexlP3mpfx+xa25mnW7WHU93627ShH4HZuD+Dcc+zbl1Cr21HKvWjZk7jvOHI7jqODuP4cyvZLusccQm/dBzyGc4/hiQdx5Vnc2WR249o4VkgSczSG2d2NeRvHPkGakCY96uW1XpyP42oaMz3OFVZFKlZtBnF/DGcbMb87cNyEp1djFo/hdzfyXI21fwBz+h6zC9RlCjRmEHckzBxySJHCPmp24koSdRkyEzjqNXvxXIeZRH/tsWt4Gk/vRKl9qPMw1sp2HDVjL/Xajzzr0eJJtEV6JMGy27CebsGVFNZcF3qwBbUYxNEAfvehpGmMajdWXDf6axLtaEeuvTjfgyd3m7vxXJ2Zwb4buc6yTsN4phV9LGv6EWwZbE2s7ySu9OLOXtyfxtaOHpnClka+ks8g6j7ENd5jduCpEWxDdq0PoQ4juGsE5fj1GUcvTCEdRn6t7MUJbENIpQVD/J3hNowSBtEPXXi6F2WMouWjTEdwtQP39nGbRJ+04ale9OSoLSXJfkniXA+uZpCOc0vi9xSeb2ZvdOPKBM5OYptBzoO42ow+3ILzPbg6iasyA4Q/TeIpaaPMnxFcldykpVJLofKduFPamMazaYzqDJ+eQf2k7VJOO+5pQO0G0NtzXgBjm2JOGRz1YiYk0aYGPNuNfkli/zpWTJz8swl3tuJaN8oQ3iH1PwHqcA/yfBxPtuOOGMatC9dq8WyKY7QB1LeKz0quHTjXQC4gvKIS9+5m/9fjl4yU8PMWtnAEZzpRVh2eKUVuIl/0484+9JzP2Ztxj/C2BOh8C1dSEleHsRf+k0FvJJHfVszvJOZhJ0qVmtRyrQ2CMzQgrcD5GGvZgNaXIb+NKK0cd8nck1ZswbUK8KFaUOZO1LeKvKeTvEu4aj37fjPubsO1Mvyu5ZrcjPJk5m4AhK93gyuXs//ayWPbyD3L2fo2y2G3AC04V05uvg4lLMO2CaVtwpU27MeQfy/WdVtgCWd9C7Yo0EqqInRDqEcCv+pJS/wj4bxdpCgxSkD1PJItbs9IuXHy5HbKWE2oQxtziJNCtTKtQLsaLJWK80wZ9knSNeHtLTiqY2lxcv0onoyh1+t5h19iPctpQds2MB+pQxOlg7g9LkM7K3EUBcrNOei3KkpEQq+ayM99aaDFVHsvUrYT7r8VT8XZHynUqI3UsgVPtFB6kPlRy2eaeU879zFKJzLPy+1o1XOetuF6BXKtwHmRbE6w1+N2E6l1ENSoi21OoR11bGuUc7UPz3cx9WdkN7Yq/O6gzNZA+W4rZ3OS9EzOV3DtNvM4yZUm45iihCgzvo2rMsryEpQB27hSKthKkXgTXLX13CfwdAdW9Bj2dfhVRamogTMogfZ0o/7teLIV9WylRN5NGbMWT9QhlTVaxytSB5FGh3CtgrOjEn20lfRA5kQVjvx7U+zFFPtcaF0FZbk+O98HSI82oeaNpNZVlP8H7BxJYH73cH5WYtX4Ml2GvZIiFUqRYshodZkrcGcjZ5HoFDJLO3GfULoOXC1HrgfRiilgBvlNWt3CL/8YnkrifCVbL7P2ZtwvtRf6muTMEIoiECoi6AdkvBvB55KkPzE+04v9GOlwOblRChx/L3LZg7U5h7lxDH02Ao54AFRXaP0s7hXaLbSoEfs9qNMwqLCs1WFc3UGqOY78t6OmU8h/BEf7cLwXv/pRU5GXpsmlZ8ltmi1/qkYuGa6/AXK9ZnKacba/GWkvKf4U9t3k/V3Me5ijmkFeg1wLIsEPkXNLr0l/jpNKCd/px/MjyNXn1E3cynAujrtb8MwL3F6C7PUQ5IzHKVe9ynk/g+tD5GJDuLsHJc9gnNLkLo3oOZmps5zJSdS/lPSqyepK5dS4oqTTTSipnRS2DZR8GPs6zudq0q1q6mvud5z7DHPxtQRZ1W08lhFMUT+p5tUuHK3jfG/CsehYfh4dQCvpdApH/TzXblENaSvDe+Ncdb2kXn657bxfZnwt76ziOhfqcx7rvIFnK/GrjlpcM3WfKO8QWiVaWwc5gH+mhZRIqEYt99Kuqjee5jqsZV81cd1WUh4SzaqWa0VmlcxxkTXrMJKt1DxbmfqrXnhkGndC/2VN2khHesgBq8mpmzgLmjj3M7y7jKVUUQtM2yfrSe+nkPbhvkrmIz1Xj7JbKP/UIi2jXtnAUYtTwxMOXM87hepKOxrYXtHtoihD6lbP8mupRVZTd5WVXs68ukGFJOdazIwmy1cqga3UUCuJJlCTJnItn/MJHZEVupljsJnjuxmbcO3NdvOPZoFmtrOS9dmMmsv5laS5PSilFDCYOxt5l+jYQzgjOUseciRadhlLEO10K+0A64AKXnUl1oMqbmCtN9vaNwCSQ5LX63C0lTS3m89LvnKmAUctvF9kTln7bSbldXE9VpMGiGwu1P9JSuRJ0vE4rzRgRrdyzoqdYJRS2Tj1hSZaU+KQ6CqxyiZJy30OOkSbSTmlzhilymHkIfLkVvL7DHSDDpx7Dvk9jrEbBEVLYC9WngrMDaGcc1wflZSC03bGNZKiNFD6GKG0Uk6ZoBe98gR6fgdX8COcZUlyNZGHu5HPNEpLcUZvJxfegr1Iv6Pok3aU0M9aN5Jrdlke4GZyP85FKSNWUIoXGUB4UDm5RQNnZhfyr2NJ/eQ1Qo93oF5Cr2OkqOsoN47h/l20xghlrUeOIsX2v/ECpZAucvIOcKU4nhD9qxqliMw/i6cn8FQC94ge0oF52IfalZIWtlFCHMGzQpN7QFslreVodFJG8+f0OJ4cQo7Ce/uo96XIx2rZG+MYj/tQmx7qQtLDlbj+G8hJ5GqRdUTCHcGdHZTARIuSlTSJM/4qPk4aNIqWl5EPdJCejoAziXVnALlXYLZsoVbQyNVZD813hv0xzdm8H/cP41cnyl5j7xkjbxG95T7KdevAKTdx7g7hmf3U/oTeC2+Vs+uQg3C2Ma63afbAQdQ0QYvXQdR7E45T1JGbaR/0+3NUpEXPgBsfAye+G7xjCnXrgv7aTwtcKetRxa0Uz3dhjGNcX7I14proDHKm7c2zW9HqrajZVrsOW/G7j6PVaPXzRo58k5Xrt7LH2vOut6C/Ypz9IlXuYt9IHgNIo6S6abZiP7YUtYQWyoZt6J0kNTeB6FCVpOdNpNIt1Ey2UgaVlqTJQYXO95EnJGhxkLX1Y281bbWiwU4i7af8Mo08OyBnjGJLUeNu53oSbbiVulmGHDhBC61oPTKCs1jVk9Sdx/FcD2fjJJ4RCaUfeco6HOTa6zNZ6vYZ9o3UtY9zq4PUO035RFZQM62SbdQXuvHkAOfJOGlIP2WyIZYd56rtJQcWa0QrMIB6JFCPQerQA9TXe6ihD1CS62FrRCbfhbL8LcG96OlCAdo599spFXVyFokFIckV0IhxK8V+HfKTkeojHxGOPoGyZ7GJJl/PPq6iLbWKFuM4eYRoNyJhTWIWdqGkOO4dRT4pjHIC7RvA3GwBzZvDc9N4boA6SzvmbQrnujkH9mGTmo8C90EinQJl3I6jJ3HfzbQyHIK8+ChW5EHM+DR+tXkRbyHksmHqxs2U/f11Jv2f5NpOklZLn/eQX0o5PbRp9NLm2s1Ruw93JtDGQ7hfLAxtnLet3ITb7EOLsqhdAs8muRrbOB8SlJJ3Yt9AiSBBmj+H432UleWspHvNblzZjXQafTQnOo23nCMxR4o4imty3wQpcSNq0oXaNpofYlRG0Yc7uF4TlOt8GaSbXHGQPG6EvCxG3tjJuiStB0GkQNH6ZWZHaakWmSPKNteSb0xwDFvBLduol8Q5M9KUaFrY9lrKMXFKFN1sn9gYOqhjxCjrCBe5G73eghz2oyX1lIXuh9ZQhzE/gN7dhfE8gXregl8PYvTEbrvfPIz+PoK2TlF6j+HpnZSt2qmBNVOT8zWBLv7qwFELbR2ihXVbbaCbV6UOm2k1aLerTCyMU9S6o5SzEuR9QudlZe5hvrKa25jPEK04nZgXGY66yBFHgCrqVTKP0uQZkETQZz1ccY3Ul/rYU23s8RRzqac+0Mfeakd/jNm1n6I8mOTMFBlUbHlj6AfRt5o5G8WyOYW6pKntN3OdddLWVoW7ZqlTt9ATJFaDCeRzHOX4FqI4ZfMecJubcW6Q67ibvb4HOWUxCs/RguT7f2KksZ0oP05OWctej9u0j9pbE+XkWvah8MYJ8EOxnk5YP0cly+1jSb7fR+ZFK+2r9XZrZ6/V0/LrzjXQolOL2S10YCetx520a4pE0kPr1gglCBmLl7wmrJo0ZZc05tMGSpMJWo+l7Edwr6z7Ma779JvULUUaPG5tA+O0FI6RW5RT1xPdppt21hncXcm7RV4YxG9fkuqgZN5hRyFOS1cn/Whx6hlCs8tR3/NoxRb5fggtGiIdHqEeOEBrZTdlILEkD5B6i/0vTn12jHpjhjSzH2ujCRTiCO2s8vseakgZ6ni91L6qybFFHukkd+kjP/a3RlpkTt5i1AuTlC5l32LPNubd04j6n/xU9ZtXkifd+datizqnrP/d5DOdHM1+csEutELsN2KXbUBLZX1spfdU1meXZ7yQ+JfMixjRxzGGR+iXeYASsUi9CWpMtVihYtWYQW6jPNNPWa4FLa/nmolzNNw+hfzLaFESe0s5NZIUdYNq5DhNGaWclFPm0lZcG7O2rwFcF7ljF+Sn+zBat6DEneYwuUEvOM4ejOkDODsNujaJkTqA8w/i183kbkJLYhizWq5DkWfK2YImUuIkbYsbKM+3k9K1465S9JxoKa0cY99/K7y0m1aqBmrpZVwHCdqMhukpTZMSbyVlFz90JXIQu2wZrQStbGkaXLzOykNRUocOWqsrUWuxgo1xfPahdbeYZm+x1+atxOrbRdlhlJRU7MVicXyVUs4YrU6iCYt3ZRR0ewdGXOjbNLnfEG0Ysoafowe4gzpXBk8d5GoUTis+qWc53j+kv+xmtCyLu46QB+40PwYfH6S1XlbGK6Bve1Ej6ByQcB/HXBEPjHiuZjA69+H8UVx/FbkcoT1qGnRunP4YsQzci6N9aGsWtXkNV8Xndw/umMEzIivfjvl1lJznGNokvq8ZUOG9GOPbwceOI9fDqNHtaMUB5HMMmEGJ95hj3mLMVPFjPsQ522qtAIco/Ur7M5ROe0gH+yjVRqmv9XCEW0mZWsghBkhfopTBuugB6KGNcoRrpY1Sm/CqmylZZdDjA7SLtNOW103Z2JdoxLoqNGKCmkcX7VDjpCxTyH2E9guhNH24o5/0cYz0b5jaQoxrogm/RBo5DmyndBCnJNWP1XkYPXMCYyS+ZaH+O9Azj1PiEK1rBr27Hf3bjHQvruzEE7uoB3RRTjpBb1OSktN+XGknlW+g/NSKHPaZLnrWerm2xVcjfrVJ6ntjpKAi24u8/Shmq0gZB3iHeGLFE3kAvS+j9Az2B9FbB1G/Q1zFPzZpyFgPmcdQhxj932XsEZEzs9jmMN5xjOL9KHkXjg6gJnPoiUF698QGLnzgaYzBfpR8GEezSI/RYrkHbd2Lc3ebPZifwxiBHnpwesiBk1zDCf5upkTQzfiCDlL6dnKEZq7mdvZMPceyhREBjZz/4PheBUp8if7KgyhzH8oepRayi/LkLaRM+8hpBqnLtVgK305LesZaxZPkGjE82Uhrs0+nU5TWusmTeiiNdNBfJ1R6knaFLkozVdTQqinRt1CbGCe9THF84xzJDnLfDC3PQv86rYTWSDtlG+1xrZzfGUqw7dQXY6RP7ewd33LfSroNOQkyfSdL7MKZXs7YLur9x5B/hnO6m3pPF+13baxrGqMg/s4Gzsx+0tAEufQA29BKnlBPqaqPsloXdc80pb9u1rGHVgXxzE2wjl30nca5cns5p7u5qrvpe4iTMrfSWpEgN05jxiRphUhzDsgMSFDWaeea9mXIZtLiUeQYw0zdgTWXoL9UbJ5CB+7D3G0lrejhKNVSUhCKIvpiF20fj3EUuzBHRa6qo7TcbG2qIn3uQD/cjXxe9UqFWnpLsFI6UIrQ7jrKO8cZA/E07RwPoaRjuPtpRlTsRHnPoJ3P4/hB9IfEAzyL9TKBXGdNP+b7ICjgHGdOHGu0neOaYZ/6vinxBXWR+qUsL09whKrJpatpM+8hp2kHd2pmf3bR2ttH7tnO9dHGI789sjJ62NMxesva6LnwbWxVpIjtlPPrrfbtR1j1WB7YbXWVOL2MldTxfe9TO7l0mn5b8S2lOaa11HwbqQE30c5YTRmhkZw7Sa9cjDYQ34ci5Y6z1SJT95P2ix+8lnq9L8eIFFHHCIROchoZ/zavBH2QxTjsQJ9OYt6/Sgk4RZ/nKOdRHy1k3bT9ZLhKJym191D6HWE/pmmJ76XO4fu646Q0wk1bqdXIrNvO1e/bQ2Rt7OOKqKONpI0zuIccp5eSbT3b3U1bZopWtF7Ss7TVyXpIsXu50pOkIymOWrv91cHaVlmJrcFKIE3UrJqpdTXRbtPJEctYD2UfV1zaeuoSlEN8j2acYznFMrqsTJdirEgbx7CDNvBhUi3fztvLeeSvTl+nTTEapZMjlKG9r4dafoLPDrDXmmjZjfHeLlK2FOvUS64uRztwJN6iJ3DfCNs6h6NWev2HSVlTbFmate63Xsx2ymZl1gv8Au48wD4RmttP/SJBGtVIuaqTFCRJ60KM8yxJatxNKVi8a01scz/ncQfbleJ9XVwng9RMa4Fd9C7NYr3HbRzGKLnnKOSZ+yyne47aaYYzroc9I3EY7bQo1VufRhs5wCBnQxp3dpDetZPvfdxK34PWB5LhGDRZDauePuME12MX/a6V1K6TlpJm2I5WaloN1j4zAjnR57kHOZeGyQPitOk9Dmq0HW2A7Io7jtNCdIJS+3OgWLfjWLabcfUYVuoI7mmlZN3FyAU/DsC36lSTvkiNoqxVjHSqkeupjz6nDvKLWlCpDvZtJylcOymD70n3rXgDVoOPkYJlaKuQe0Zpx26yvjCRPFO+/sP4jDb2R5xxHrV2DqfJadpopWlCa1No7y56HEQC2MfZch/uGAStFtmgjWM1g6MUZ8UJ5LAT6Xa0eYajtJ2j0UfOP0JeIVqY2AGmuOJGqCl3k6JUsXyZfeOkq13UartoYWumzjTMlSCW1b30tveRMjXyqVr2XjM9vn3UAieQJsjX+hm7M0jLTD9nWR/l3lH82s0VOUE7aBstKn5sU5elg9spX0yRjg7SZjJA23kfdfdmRiMI5+7iqk+zbLHkVHFVdZJHDnKUmsndpXXtHGORNju4xlKUj/yIqRG7CitJd1spIdRb+1sLeqGPslInOXiCdRuh9NJCTthlPo+Sh6kTdtjcOji7O1D3HaRa7ZQTNtO+0WatJ0n6EjKU54R7PIfRvplS+QnWoZU6fx0l91HKFEK/qq1tXWbmAHpqlFbpOcbN+FFK44ysTbHmtYz26oVcKXOhwcoLXbQhCh2f4Dj1k2/VkRqlSNuO0h40BE7VT/rczujnDnKQJGMX2lCretKFYc6BPtIJXyppo1/VpwKNnCdSqxStivXUXqppm0tZy1sCLW6nvrOVa6WTa6WW3iZf42kg/+8mj4hTthIJ/GWs/yb02iDQRwkqzhYfwbUJ0s5h0qAWaw/vIX1vJw/sZc3qGeEmNsaEF/OmSMVHqE1NclR2cMz76RVMYT3vwLMSM3cA18W2I/03RNq5g7RKfIiT9AxKJOIM6jWFdTNBLtFLLa6PcYHDtJqMck6Jl2EKe/EdHqLVqp+/OmhXGsS1OEe0k5pKK1dlipJmhnfMWV+WxDYcw34K9RaJcILSewtm/D78ipNfiDY3aiPPMqTdfaQWc7zWSRv3MFdgHOck5rEaaR/LbKVVrYXayAg1qyGuXrGbiNR9ED06hbXdQQ2w29LNUaSTtNOM4o42rtpByvPd7AnRTsvRq1NslczFnbijj/QiRlmnj9LHHGNOatkPPdQwpe8k7rHOcp7tIlNxBHayHd2mgp6ACtroRhib18nnZ7jyksxdPLi91DdlZg/TKtTPWT1D2and2la7WWIb6ecwZ43M9iRp0ATlpAypUgs1iEHL67uoW0s86AD53xClgR6uXxnbXuYaY/2bKYmOcI0kqdd3ksolSW8baWNp5W/xEHVShtpuJU2RJIZYn25KBX2M4mrB9RFKNf1WPxUqGuPMEf49SfuJ7yFuYW/3MfamipalJq7CGDXVYdpCk/Qd+tEpEhvXh96TGJQM432a2aM+l6wDz/IjVrvoSxunz2mIrfYtpiO0//Zz9g9aOVZ0vFn6tPwo1yFahIdJ8Ubwe5L0spP3DVNqyFiPnO/Rl0jWMT6VYjmDb5Y4Qi2+l/68l7h2+5hjH0vpJ1/bwXHxNeRRrot2+ib7yccGaf3tx10zdl6PU0oeoI7ewzv82FOpu8Qi97CV/bQldXJuy0iNcgX0sGeqrUcyQ3vfoK1HnPZF8VXvxXgdsXO9nNL6ECOh2skl0lb3HqafcIQ6Tj/rvZXtaLU2mSTndBv5YD31jV7aj0SH8OltLWdQC6WBbtKZIVrp2ljvLvoVhqnJiLd5gGM/QAnS56Cd9Lh20p6ToP0zzfXg29x3MLIoRlovNK/O6tkp8s8J6topyiFCSwZIsXt4r3DpBEtqYZyDeIySlOw7Wc9e5tzCUUjSCtFJjpPgmh7k2k1SzpJy4tRceymnZEh/4/SItXAGp0kxB0HZxbp4H7ngoJWhBtiyIdsvw4yvkxL68va5Lfd70u7FH+BrblLqAH91UWZvZb+3kCo00KY7yHo10JJfQdmxjj3fQPtRM730UWpsQs8r8IzoXKI/VVITaOeo9dBXX87co5RJfZmkmfFgbg03U3NqYnxqA/upkV6HhPEjXP13ixogh75OW+4rkE1fhC7zKrZH+H7CPeZBrKdHoRsdw7kj0G0fhgx/N+59wVtOS+we3CFvkBymXWEf3yp4FrQ4yXJGrcUmRUrYS1k6bT1jKeq4fqTAKOXAFCWUdsrB9ahVH63rftx9F9vca+f3ACNrxjneQyhb5J2D9CqM4KmHqbe0k2+fsBRriPQhQY4oVD7KKFzfa9rMfhzkr5i1USRoZY3RBlZP+h+jLtmOJ8XnJLbZ1ZRffNpdQYtEs41zqKAFP0VtvYO6dcL681rp+2+0sWD9lEK3oHV+BHYn45Z6aE2YpD9iklR3mOtN+P4UvYQJri6Zty8wcnEvct1EP9thtCpK2h6lha+e87+Oz7daD2w75U4/lriJnKnF+JHHKXLwQbMOvw+RzqbpIZ6mFreDo5BiSydoZRTaXE6+NULNUahlKePNtpJLZshrBhif7NskYzZepIkWokpyLcZ5sOxJxm90USqLkk5O0SORodwywtXXx1a0M/5jzuzn2RnawJq8lVzN200z33vw34bIIkexVQ2wxlu9OvpXeijztFDHK+f6dHJ8O98KaORaLmUcW6nx3zoT2XE1Z0EHvY5drEcduXaM8kAHJblm2h4a6ddJ0wtVSUmlmXk30NeZNBdZj7zMkC3Uq+WtlS3UpKuox5Yz4i5Dzc1/A8efjVOkqBIvIlEbXbRutXDORc2IF+KakdiLOm+xtxgrYIjxRwPkd3WMpU1wNVWbV70SbxXk7IesrVMi4CT6YAejZOTNyGdwdhfHJsqZ3mitGp3o5Sms+dcxBj1W8hfrocgux2ltaQX9eImemkPgKrPeGtTsWfOMdx5mk/iN5G2eXbg69WakWyVtEhvIFbACvQocZ5BHnCv/AH0oT6OeJ6C/SJRUo7kfc+Ax0q6bQZWeN2vQkofR9jE89RBplVD7FzAHZnH3fTi6F1sWeTxG+f0wbQ7HkPNB0JwGHO/DXceRJpDnIVof5sxxbwmOHgP9E249hxyeQHlP49dLeOoBvol0M9KD6ONjjC2eQw0PonaHUcY99G3tR27PoRbyntpx/HoS18TrsI93iefqMdT4eZSTZX676Kk6Qt/EUfwWv4nUZx9KzGJ/BGO011uHdXO/edmLINdH0br7QMFfxplB48enpqgZD5BDbaft4CA5X5pjPUpO0En7XYI2PaEHFZyRoq2PobXDjB0apCbcQylymLJyG62QUWos1bTtt3AuioSbpF0wzTyGyeuaaHftpte9jzpND8t+3EZxlpO/N6GUMuqPI7RfyIqZoG2jk6srQ+lwlFJwH2lwGi1qo6QzRmk8Rbm5h08LDY0yKneINCtFiS9D60jG+O+O1NKeMEiLZdxShQQ1pkn071HwoCP0rs0xAmQCZzowSn2+X4Oxim2kIClKN72Ya9sZ/yTUsA7PjlDLacbIiIST4LweZcxEG22E9fTeDltLa1x8YlgfCcytxzGPWjDTOoDn+B6fRNO3YDbswvjfjTE+hLE8Qm/6i3h6P98vewZnJApH3kJ9EnV5gtTiab4t+TAl0QO4s44e4E60cR/uuhnnduO5o7j7EO3/z2MezVDnfYy6cZaetQ5qcRn2hWgP+3FW5tKE6fWSfHdvJ2SEY8jjOGfSM6TbYuUSf9sYSkibY149ZqxwB99i61tV/fdbhBb7tv5GRjBX0ZeQAP3tYcRxnHVooXwwTD1A4lIvEorjXURpf4RWnwdppZKYPumRu/mm2hg1ffdGzCbKzS2MeI5RihXdVN4f9uPyG2grF0qZooxWRathjH60JO1CTaytyKznUYJupoRfxxFtovZUylVSTxtHlPanAa6PXuPHtNUy4jpOX1yGuTZQam+mZJhgbk3kITFKMBXkmjFGi2+gfN1CS009n04xIkE0eD9Sc5qroc5GJ8YZU1VG2auFLWxjhEwTZc0yWn66ae/fwjsG7dxswu84fXIDLL2Z8lsbedpFrKtYXEpp2/PtOk2MH5sD1angW08VtNi2cH6Xst+quZqbyM3b2Ufd5K4tfAvet7r5OrmvjSdpi5Q4ilb6zHop8/lvgUivbicv6qaNNMNZ0kFJOG7f5m6ysacpSkFJtkckh1bjR6mKFW3U+udibGE7+WyMsk+M1mI/zrqF8kojJZoW6i2PUmOppj5UTn9pBa1q8gbWYyyn3vhvfZzge9jiZ38Uq7mT7wg/jCf24sxDOPMo1sndWCWt6Ltj4AePokU3k0ccQa8cBA0QW4JI2MexrsQn3ky90X+HbYgStbw/I/R4jDFulZR5/XdCOkmlevnGXhN7pI3+2XLOqAxb00ffShdpbg+pl7w30E4fWS3peSt9X+K52Mg1MUrbcyXlypT1VojsV029p5q01fcOVlnfXjlmbhX1lQ18Z6ERd8tKK6cPsI6yVCXHsA1lRCmTNpEjSWz1RYxGr+MbCjEej3JOtPI9kDJa8DeaVVjdItNX0EJSBYmgnu+/rGLcdIXx32hMsB4NjL6SeVdNz3iMUlwdr2+2MXVtOCrlTM6w9GrKWQ08khVSxvdffBt8O3lCtfHf1fPfUfQjeDo4E+O8LhzhMHW+cVpYJKbqGVI9sfXUWT1lkr6BPrSmlb6IHq7IfXh2L8b1PsyAFr6n1YWjNtqAexi3I3z5JXLeRs6MOupBadqK0hzrFHldhvbHFvLjblqIuhkxV02LQit5YYrcvYx8WyySUdr6Y7R0ddAm0M/aSyyerCLxIddTikvSXtBEDWeM/SGl1tMGX0fKn+a7dAnKdAnycP89xF7qGE3UWIQyjjEmwZcIY7QzJEixN3PGCW2o4FEbdege1q3vTT9gHeWLBko7TZypnbSipElxt9Km4EdTZ0hHurjy01wV5aSorfSNTNAW2UHpXdbYNLWDTupnbfRzpWivL0UOe0lX+qglNPF80vjvXmYohzSQd8RpO+i0kQR9lIYSHKc025AwKRt1Uk9vVjejMie4llOUsHyb/oPkP92cqRnaZJo4L5toH4xbP7PvwWulz056bR3b1kU9N0nu18a35tv5joVvE+ynlhNjjRuYXy/nxRpaV3poWWmn3aKf6zvOSO1+cpsy5iMWjwH6FX2vq7yVJ2t2u10NW+lHaKVfoJ4+hkbyI98L0UhK1USeMsG6lZHX+XEbMbY/SYmtipHuvsdTpLhZ3ic9PkqqlqCtqZvem05aaUc561JsQSdb3chVVE+q5XOBNPKVdlZhnURJI2LkgzJPjlFu8UfLt4VV833HEc6dw9ZmFWNcmKy1e43Ec92Pmk3zjZvb6UOoZ4yXxNg0UJaQt/Lvh77QSTn/COSyWyCZ+X4NoRcVxo/PlVm1hV8hkTW+2vjv1onMleHbWqJ71pNzpTCLSylr17Kf+qnFdtOH7UeHJ200j0h5T1NL8ClAi52DwxxT/5sOg+BAj0PPqQeFOQKOey99IY/xbVKJkr+f/pqn+YWKE9TQ7sb9t0MiHIZ0eQLp3djkqUMorRpn9uOZOPJqtlG4m/hWmv+dlkZy7QpKXzHy6QzlmQa2u93431lJYj5G+SZJLTnMRsaBxilpxBmRV02beQtXdoyUu43UezW5TSepTwM4km+92WLOIZeU1R21b9uA7kF3nWDf+d7fcnrBSsmhmtmPYq+opISR5ttKdZzHdZSbKujdS3A1tdFz3IAeuZseZemlCba+lVqu6Dd30/p3M+bKII77cDRBeT1O+eIIOc0ult9NC//dpB9bca4Rd/pca4RaX4oSgO9jEQ3rbo6ozKTDOHvQWpkTyLOCbzFtpM+rgzS0mfNZVu0OzucmvrnZapJWBq7jr37aoTqoHXSwNmu4Zmrx1EHGT2RoM2jmikxwblYy0ryb49pM6arbxu800MpRYa1qdaTYfrxNO+epjPqI9em3kr/7tqsy9vM59h1vsZQ+QD9DipE7zZzBPjcaoFdsDddCLS3A/dav479rOEbrcpKRHRlKonHqawNcJW0c4Qbakrope6apPVQx2sCXhCqtTltvrSJJzuAE5ZFm6rYttPMOMqpLZNsd7NtW8g3p8w5GlQh9rKSPuZnSfyPnfox+vWbqL360jcSTTVLa9L9mkuSo+rbdnfQ+Txr/rSt572gf1t4J2u1E2hDtYzMpsNCWrSy/mT3sy4yz7OsY50gzLYMttBV00ecSZ3uixv9uQJx2rRhXXBt7yf+uS9R+eaKbHqUUJdla630XbioelEFaB4VKb7d2yA7jx9Z2Mqqgi/0b5wg0WA0qQSmildr9frPdu52WDl+a2Uo7QhS0p5N+vi5KqGOcs0OkK35MUyNtAv5blymuFv8t7igtxH1cB90ssY8R6uWkSEPApLUopKjdddK+KVbGfvLdKLXHOn4rSnpWvh0iscGVoHA+x5DvBgzzSKxcaa6GCnKBzZSM5Iqcf4i+ygTf691i6ZFQlw3UlvzfMb4NU0VppZFvHrdR0q2zY7GRWmUFvw1SQQpUTZ+UrGLZ/5jrfND433GppEf/ECMQmmhJ2cr1WclR8DmRUJgeygUVXPNp47+jmTZ+VF099b0YozJ7OHNTfAvfl3YaqCl1cxySlDn99+v9L5fN0Jo8hNGSa/787WZ0rLzJIdbu7dT7YvSKtdsIwRnjR82J72aMEVXD9M84H/4AdUnfjz9DeUjs2Xu54nuRm0TcDJA7pyh/9Bo/dn6as86PskiRDg8xckX8x0fIF3tI84fp5TxEGbodo9xP//wENaFB6zWc5hs/nbSXd9OeL/rsDkZg+NLhEDm2/wWeTvrIWunt7aTXs4fvJnTwVwc5RpK23DmToA2pk/JQnNxrB7+eJFKHWHYl0nuI3kU/wmKIYzrEyLI28vgh9k4nI3BGKMl10HIwzHE+gaOdmE1DXDVJevznOGdE9+zhuwwy6r5XNsN67yVvHTL+29V7cfdO6sryztxu+sfq6bdIUM5o5T1D9ET6Ul4D3xfey/eV6/mu3V5GJrSR58h4zBqJsZuiXDlCqpjGL/ErTNNXuxucs4t93c2x2E0+uIWeZJnBY+RDnaSVHdSTx2w0wzq+6yBe51nGRAxSA4lR0h5l3ttpoSmn/tnAHq9jTMMErkzT1ioWQ/Hl7MSVacY/HKDF1W9dmhZ2P960i9y2Di0dopyapp9XIsQmaGHrYUxGN/lSK++UqzKzpsgfR2ldmkBrBmgTqSbNGOc78bUcx0H2S9J6oFLU67dzjFvYy5XUTCpoX0yyl11cRdT6uTMsq4ccLcN1Pm6jW2ZI18RiI5EzGSt5pI3/1tAAab20a4w9PmijzHpp7/S/4zSOcUqSL+6kfjLB+dxBnpayWu4I53uz9fbMURJrJxXaSY+4H/OdIXXqpAW2nzEso8y5j5RAVnMb5cBucrR6rs9RrhuRwKaoLzfzmx3tuHeEFGSU0nmcvL6SvKeBWkUnZcxWShxVlLWTnAPCuTeTE/SQIsYw7s3ULP14Sf9djBbQiB1cTR2myXrrmyh593KFC5Xwv43hr+skc++gBF9Pi32MOSVoQbmFdLXS6sEyu+RIdJkJ8uJ602kp93ZGyLXyPbp+G52SIvfYZG0VzfRnbmFaSZtxNevfQznaj5nz/YW+T7OfUkmaNpkGq5+10sJdxnlWTT4vUnIXbTcio2dIs+ut5eQ8jM8WrqQ6xl748Sf1tCUMMU63lv7kbka8+RLYBPW4QeQofFPsqnX0xEkO9eS75cb/ble5bX0TbUsttIuN0sbQxLtHMd8TNmplB+OzJkgP/W+ljeDqON/UyFAjHuTsGWPkyZR9g7yHMSz+Khyi33EHfb49jFfqtbElfbxnis8Oc3522riSQXK5UXK9VlLyIUg0fvSK/+ZMD9dVH73dQ2ztAN9CjRn/+2/+GhX7j0SfpK3Hp5HSRT9tOR204lYxztOPK3nG+O99yDoq5zr0Lbj9VqPttJEo/jf4xGpSTQ21g17wuLWbNZIelVMWL7UWXP9LXa0mY1dwklaedkYf+e9n1NrIVN8TUmG1hwQl0ARHv51yuMg0W6kLiubkf1VTdOA9lFObyTv8d+ulfb6duY7jMsFIjnHGm2QYXTbC9g3Ql9pp/LhYPy5J3r1/iN7d7aRY/tvi1ZTz+uxWTVQz/dlztfatHHf+5Lu22Lui5DIdxv9qT7eVfIRSVLD9jdZLPszvrvgxHeVcizFK1kmbTzV7eAPlzUYbf+AfNVh5vZHfZBULbgNtM43UsbbwCV9G3GTPNuKog5Zqvwc7+O5CHd/jehVz7D7Lm1vp2e1mVKbQwClGInYb/1trneZBjqz02iOgyG2MVo7TkutH62Soow4hdz+uf4Ty0TTzEOkozbioUeowQ4RITX20ArZSixPtbxyzN2OO8v4e2k+HKMl3M5Krh9K9SFUHyKN8e1jaxuf0c42MUbsSr28jrVLDpMYN5JRNpLlxUrFW0r0EJee49UbEaO1qJx8WiXgjfQqt1FabSYnrSbP977i00fshZVXSJtrEliS5epqpm5Zbn0IDNfI6Wp/KqdE283tycWs5j3GlxqxVppbapYz5Zvvd4DaukTh5ewu9RY38woz/5bAq0tRG69ts5zh3cYa0cq35FLqNdr8o3/SRdo7xa7CvmGf8r8qaR3HuPnqvH+Y7dg9xe4Qx/S9APrzfPG6e5zdSp3FG3oc5ht8Pm3tx/CS/NjvFr7bKd0bFOjhKS0ovrTSyltdx1vbaOEzhWgepvfYxwlTs1gc4P9qN/xVR/yvEc5hPtaRZk7Srpdiv/nfFhErusxRzij72QRubMWbyv1Ppx391My5YpMhJRpYN0LvRTim83+qduznWL6LndpCu+XO/jWt8K+e8/zVF/92Xbn7vLfcFxTrOMt8f1EONyPcs+T6MdmpjftyR7ynwo49ixv9eof9UhlJam/HjkHzu2UDfX4xyQYxxSUn7TaoW43ve643/XeM4JaWYnT0ttHnVM3LJ/ypVM79m6EcwybcMo/zaVBV7WFZCFetbR97dQc9BB1vWwC1K6aidsSwiqY2RNvsjInaGWq7mDH3lCfpC44xc2cLadEGb2sW4iU5K7mOM4N2O9Z7Erz3Ue6TH5F2EcWoJ+d+ck9iWCVp/M7TvDdLSIzaSRzEbR8kzhugb8+Nky+lZmWXLtxr/S25b6Q8qpzWxgS3OcMT8d9xaaMOrtr6nOrY2ZfwvUwg/KzX+lzVE2zjK0UzTKtVEWaiPlLbJem0rORaDHJcWOycaSYH8txCbqGmkaJ8Xyel2cw/WR7eNlkgwvrvRWlyqIft1UCdKke6Ibp5k7UV3uwdjXEv5SCKADlFm7GKERgfjqH7IGmTI68pJfdOUCWqpZ9XTj1TOODn/W0ADJms68r4uVG/a3rK1sG7+23ViXajlvOykxaOaXwprISXr4QiKxf7HlA0kVvso9ZeE8b8k12b8L/jci3GeYGTfhN1O5O1z237qDWmuwR6OtHDSAco1XYz6E427jF9vPsJ4hyMFbY8U+dx9eKKY7XSXd5xfBZqllaKQrdjy9liNftqmp7qNFHi/25oY9TJGi08hW8Z+B7nbpqe6DdnnZwosb6TIrbhe2YvVtYvbfpue6hbHyE1z9KYL2noL7A+37Siyff38gqnMtj0FbV223MkC63mU2qJ8AWq0oC1h1+9Qgeu9hRFOk4znKmR7u5QXo/zeTatLIdvpLm+oyPnZxTg1sbtOFbRN2vXUX+D622Pn6VSB87rY9hW77bHzZl+B80xsXNtJ1bYXtI1Zur2nQDp/s63v/oLpUnH9MmHp72yB9LrPvMbtUZue6vZkcZu3rNgn3yblLT/N5W0s8sliJYOkXfezBdKJNvv8TIHljRQpF5xuujRV5HOjfEtlBvLBTEHbtG3neIH9Ury8VNxzk5YvjRfGx7xVxW2/xFp6uahNy/u3Le9006Vhm3YX+NzbhS5N8ouzc7SiFbJ18SsWQi12FLS9XejSRJHj97J5vajt7iK3+4vctLyfV15xzxW7bh3/6y9QjxsrUo87WOS8Hvw/SoV+dpux+tVogfqY0+MmC3xu9C124VPdXH1Plx633dpPxwu0tz5iue79BXLpl8zD3J6w6aluLxV4/1ufO93lFf5csdvpLa9YeanL2kVmC7SjOHlpoED6Uqy8VLwFu7htqkA5KScv/cfW47YXN37ehiI1uRC/N/KLt5dO/n3Kz71a5HP/1uU9+3+onv/O5f0SdGk3twmbnuo2bJ8v1L7k6Fmh6+/tZl+6pUC6lCnQ3u22twtd2uzVyoYZXvu/3Tac/PuU5/9Lbz3zC8r5v6S8otc7KEVx2+kt721Dl0bsehorcP2dfnnJ9yPNFeh3cnRprmB56e1Cl4qqp6NLXkWBdOKEebSo7T/6c8VFIR097eUVK09kirQv9Rf5XKF637+XvOTsS+MF2okyRdqXHJ2eKHDFu/qeLvvSZJH2pafNj2XzFvrpKW8p/g/I/Zg3+wva2iyfGCmQr7jyRk5TeYNFlpewzw8UWF6iyP5MGP//tydteqpbsfLEkJV/ZgqUl3rseuovcP11vW3kpZ3c5mx6qtuote+PFOgPGC1SXsra+p4uulRofJXbjvLbso/x/9wUsm2y8bEzBcbTltqv/Ha95fu/v2grvrwh+3bfUEHbpgLlardt4ltunXwrrpBtk+kqanPlxQssr9h122LHY6zA8Wuy8bipAuN3J4uc18W270CRz+23fH6iQLkgU7Q8WFy/HHizvqeHLrk4+PGC5ZDC/Cpue8I8x+0Bm57q9qg5zm2fTU91e+2XLO9QgeUdt/HUhwqMvz5kirMznO7yfln7UqHy0nCR74V0FbluT7e8tLPIcei28s9wgfKSsy8VKjedfvuS386JwvrFW27j7J4oMC5vpznIbYdNT3WT/216M7/JfnNB2+kub7Lo8vx10VfgOhqw8sF0gfJEk12/3afpTbCxIulSusjnugpcd7+svFTsNv1L6nFzBdMlP85gtsC4hLeLHvdCke+hxOy3UjpteqpbnfH/X0SXTU91O93ltVh6Uej7kcW3r4dbr01PdauzX7BL2vRUt94i51mnnTczBc6zThunPFFgXHOhdt1fli6dbj2u0Pc23Xa69bi+IvulWDvYnLWn9Bdofzls581YgfNs/5vxv4X5ZQ7bcRwrcNz3F1nPw3b8JwqcLztsvxa6ng6/uZ4Ke25Hgff/suu2WHmpv0h7XZF+5n8Heak4Pa54eam4eXa65aVi4xmet/8B+X6bnup2s41rnyo4Dr6452ZPc3mPFPncvf9O5Y0V+Fyx69bZoQuVlyaLpGe3n2a6lC3yuWLp0nCRdKlYeu3ad0+B7dteZL+MW71lvEA958fWH3t3gf7b50/z9sPTXuJ/7K3YddtdIF1xW1uRzxX6vaZfli4Vq8fNWfl8V4HyfF+R7/UUS5dOvz+uqHp6xlvIzaWnuL0pL+0pkE87+/WeAu3Jb5fynD59sMCRmLXfv53M+xbuqWzFlle8vFQcfRm06fbTRJdOd1zldqt37ipQTx0pUl4qVn939f2/nC6ZQ+ZwUdvdeLKYbW+Rz53u8op9bsrGBw0VGE90i5XrDxWoB6QLLOeXjV8qVh8bLvK52SLn9f8r9qVCt2L11K4inyuWLt1sHuR2j01PdTPmiDfmBbzF5ijSjV6Zt887aF73VmOr9FYyvR3bVm+1ecmb8WJeO+447h329pojgYV4ZgxPviRpYElguaS5LbAysNIb927xNnhPeLu8yYAJrMIZ/62LI/ZNCvlWraT8hqzXaV7xAkhHvc2oy4g36JVArlvmXYR6LPTWeV3YtngbzBOoyXleBPtRnJGotn4zzq8CzbzpPZevocqXTcW/Jr/38HiSX/U8wv/50HlSKl+LdKncP8X3l/pp28+t532M5RhnbvttObP0hP6ibzfsZfmzRt52cHVym7yJPpbHB6cgA7vvY7nvOv2vUpen+yKSe17W1Q5ey+Up1923bdw3Wd6ayuaX32/bnsvT//rHJOVJ13ahN/IVi9wXIp446RsJLvX7bta4LxHk6jn3M23Hdbb9Z98ifPakVO7dzbrMnPS835+3nCRXSJ61uTctT0rtW41M8/Mcz6vnLtZz7qQ8cSxvfv3c94pc6vpz6mf6c5z9Of6W/pS3NvbzTYMRvqmQSwd4voP/3WcvucN2zvtcnjtJGUdsPfe7ejLiWqKZe/nl8BlGKIuc12njejv5P0X20m8tUbP57Zw1/teX8vOUFSB6qEQiShSjnx6ykXhzb6aunrImx97Sn9P83x6jeecmGOkkUT0SEZSfjlGnmGYkjctzt7WOntx2P+oiv+2yNiUCoJ3/r8pPx/i/i7roOU/b/hRrq/Mc57d90lpj8/MUD+MO439/dZbeuDGuDN+DMfJmHrutPJ2f5y5bz/xxl+viHfAt0z8PY2/W861tz+Up53yr4yDn192/0Bbk19PfTm77Lq7Zt7RdtHJqoAepheZS0fb62FcH8/LcflKeoonk6plru8jI/yv5UCTDXUw7f+64n9z2N9cmuaH7q/u3g9cG/KUxwQuMCR0xJvw5Y0peNCZy2JgFs8YswvXF/78xZ77752PJ7xqz9IAxy2405qyOt+Avcji70cc5h3NYcX0O5z5vzHkNP4vzL/nfY+U+Y96B/FftMeaCbxuzGu25EPmtedmYtajX2pdzWIf6rk8bc9FlwCvGbLjEAs9szBhzSQT4Cx/v+pyPdy9TKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKh+HfGtxUKheLtj0t/X6FQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoXiF+Oy5v+YuPx7CoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUPy/h63PKxQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUKhUCgUCoVCoVAoFAqFQqFQKBQKhUJxWuEZ8+37vF6z0fy1WWwCZpnZYj5szLLSi79lwsbbttAz3oVevWcWeLeH5o0J/ZnZ5o3PL18V3XbUG9/24ciC6JNPnXveBd99BLuhz5y76pqhnqGRoWD10FVDgaHPrHzov+P8pwew674eu9/swe7a685dddV111zXc91XrguZa0euveHaw9eGHrzWu/a6kd96x1U1qwKXSr2wXwasBZ4CXgTC+FVqtgEBc5l3BapyhWkGgt4VXnn2nBUX3Opd7m3d9h4cfaofu9/owq7zk9h9InPOqpHMdObBzJOZ0GWf8NZ8wvtEZrz3HSv7zv3tK1euGwQCt77xVMn6+TOXRi87WrJu/qyzo++pubTkAlTl6pL3mu8CAbMIv88sWQdsMauRrka6GSgteY+ZLHm3mQXuwT0LTbTkMjz57pJLzNdLNppv4NcRpLchvR8oKfmVkpXZQOm2oyWrsouXRu8oWVWywhjks7lkeTZYuvZoybnZc87H+YqS81Buacn5JedlQ6UtNQvw2zPbsf8yr6wtOW/+0suieOC8+dVr/XT5OdFS3FhuKoEAbj7beCXnIONAaWvNypLl+LW65MKSNWZxyZKSpSXLkG4qeVdJKZp1UcnFJRvMmaauJIgKB6WA8AvZ5SujNctLAuHXTQhVWRD+oQma0vA/2PRVm/4k/CxKWHs0/Oz8uauiH7gt/Cxqujb8Rva8ldE7wi+Gf8C7Xg7/wL/rB9nNl0Vrzi0pCT/FFkaQSg+cgVRu/ClSFBd+zb/+xlPh788vXoIWhp+av2ijny4/L7qo5sLw35nPAQFzVfgJcw0QCP8o/Hz4783i8PfCT4T/B2b2lvD3jBf+l/Ar4X81Z4b/MfxS+J+Q3hI+kg2Xfrvm7PAR810gYN4V3mMuDt9srgCaw18xSeB6oMRsC986v+Id0VU1C8O7THV4tzkS/ob5ZyBkLgvvml+xEtMmvC9bsQ1dFT4c/pLUObzPpl+26axNvxS+EaOMB2ayK1ZFMfHCM/NnnSM5/MH8snOidcfDf4C+GwwfRKUPhneiwz5Ysyi801wNXAsEzU3Ye298J/zF+aVnYaIuCmfxwO/IPnwofCM78OssBP1043xFNMp07XopY0cWZUihO2S2L6p5R/hbMstlH74pPBf+Q3Tc74enwp9Hxy0K34SzfxQ+EP5jdNhXw3vDXzNnvnFX+LPzG0uj4ZrF4c/i0X/mfmH4U+YTQCD8yfAHsheuW1VzXviT5mPAtcAQMAmEzTvDv2Eqw93mg8AncPwlIIz+Tc4vWhEdui3chQL7w83+LPno/OXvkbp/NIvZfUf4N8PN7MDmcMzvwGuyS87C+WvCCeRRGr4qHMcyuemOcNx8A5Dpe+38ug2Sw7Xzy8+VNJ299PLoreFPhJuQw+/chgdlliayq9+Js7Hwh1Du+UeRXD4UrfmV8HXhHrMkfH241yw1+GW+DPwxcT1GXvZH8StgPoP9p/BrEukue08YE/I6TMjr0AHXmY/ziaU4WgOUApWAnPk1sz+cQR7bwr+WRR1rPhT+SLg1/J8wCvXhXws3YBRKwh9BLUPhejwn+AjK+oj5OhA292P/Vzj7I6RB5PaRN+/5IHA1jpNIh5B+g/iIWRD+z+FU+OMYz6vDHeGPYbmvCl+NqX+1iQINQAjLYRtKrAtXYWlVmS8CQfTSlVnM81vD7wtfhHWDvnzX/IVro+it0vm166INd4YvwdBtCl/ModgY3uDfdHl27QY8dDF+czpumI9WykBsyK69KIrlVBZeZy4368KXvZmWYQwX3REuQ7+VYTqtZ3EfrSkLX2Q+CQTCm8OXhregfy4MrwmvRRoNvzdcifZsDV8RLkd7FoY3o/YLQi+a3w69bH4P+NvwAvNjIGzK8ev9wC7gj3HHLTj7r+FzsMTXhv55/h0XRMPHQz/G0+8P/TNnxor5S8uiC2reGz7brAQC5tPhpWZ7eAWO3hv6ewziUnT0Ugz82Vh/KzBBFmJxnm3OCC8JPc+5eqZNFyOVdRmxaYlNw0iF0AX9+0L/4J8PPR/6W3TYp2tWhJexOq+ZjwCB8LLQ3+J3aTiEVJ4LIJX7/w73G3TTO3l9O/e7sD8IBEL/GHop9E9mcej7oWdCP8CU+kDo++ajQCD009DroTfMmaF/Cb0S+lfpvNCD5pbQX5rAG0+F/jJ78QYhFTi4YLU9WLwsWvOu0PdCjwu5Dj0e+nOmfxN6iOmjoW8x/e+hP5XahR6y6b2hLFt3R+gepv81JBSrNPQXuC61z4a+lY2ULqy5IPTXxgv9NepwBs4+Fvo2r/5V6L8xl/+GuzG5Qn9un7odpUl6nE+vPYoEq71mSehO3FCCC7fY4m+16dHQn2Jy/UrNWfjtheZDR8wSsxSSzRqgAQiG/ix0Amt9WWjB/EXvjIZqzg591ZwD3A88DvwI+AlQYkLYtwCBN+4KfXV++fnRZTUrQntNM3ADcBMQMndh/x3gZSAY2hP6ilmJsr4S/El2yZqRmneEdptp4CvAN4E7gQeBEtwzh7Nz6KqLQ39oPgd8Fwi+8Z3Q7PyCJdGr8egsTs+iPrPmRSBkFoV2mVUAiF5op9kGJIHrgRuAcOhLoTOyH1x3Ts1FoWmzHkgDQTR0GvdPm8vsmd8CbgBuBPYAh4EFaMyM+QYQMPtDX0TH3Rhan33XmkU1a0JfQJlfQMd+wVQCNwHfAEpOOnsbEMKZ7TizHXlcHfo95DEZWpJdvebl20K/L+ssNDV/3uromRi6z+POz+POz+PZz5thYBIoQS//7vzC5VFTszT0u0INQxOmDmgBvgj8AAiH/jh0IHvxmutrlocO4J4bub8i9Fnc9VnzKeCLwBEgjAYPZxt+PXpHaDi03pyPDh8OfTy7aU26ZlnoM7j1M6jn57D/Io++GBpFb4yybz+XPf8CPPa50BI+NoZmbFqztGZj6NN47NMo89MY8U+bp4Aw5lY/atmPK/0Y//2hAY7/1206hPRCpL9t00Gb/n+hgeyFa+ow+QZQ8wFWZQAt+VGoB/tF2K8CSoEgluT18wvOjF5b0xb6LTMEBMwHQ33osz7z98BPgBBmcB8y6kM7+jDmV4d+01wLBDCrr8OsFgZmQtdiLlyLo3ToNzBdfwNH38X+Bzy6OtSJJzpxvhPPXxP6pAgMoYw5FhLGdVXod0wP8BUALAj7LcCNwJ3Ak0AYHfBxPHMT9t8AhLak5pe+I1pVc0noGoxQEpW+Bj2VBP4ziroGTbkGjbgGj1yDCRgKdaARHVgNHWZf6GMYw4+h8h2ofAd6pcNEMNHbOY8+Or9gcfSmO0MfRUEfxdT7KPrortAl2Us2RUEaL8Rgr0cPr0G6BulapFuQrkPah3QD0nchvQjpJqTvRLoY6UakMmKX+Cmqvz4L6fOO0HpMg2acuCt0ji1iIc5IEYuQShGLkV6G9EybLkX6R0iXIX0v0rOQSlHLkUpRZyOVos7BxFqxZtFt+OmhvEtFrgeFW5IFqbk1+K/BVzBFltZcF/wnszT4L8ArZg2OtxA/Bv4FeAUd9UcYwz+CSrI2+Krxgv8cfNmsCL6CqyvMIlz3zHRQOEU19lcB1wA9wE3ANwCQouBRXD83+KfmU0DA/A729/Poy8EHkOMzwT8RGhz8fvBvmD5tf/8Pmz4SPCQUP/hdmz5o02PBP2P6Lfv7z4PfZnqr//uNp4KHssvPjt4RPISMSnjiB9nyqHAhHKzfiIPvBX8wv/Rs9Erwb+YvrZX0/vkL1kfTNQuDz6K2z5pA8J7gCakFnjmRXXUhH/52tnQzDr6DM4vPArMIPm5r+tdIpQYP2/Sh4J9QCkWCihwPfjN4mL32TeMFPpK98p3rahYEmgLNwlgCHww0MW2Yv/LCddtqFgUaRH7CvgX4IoBZiIsLFkV/VLM4UI8cmgN1wuOQQ53wtDe+E6jLnr9SKhaoyS5AwwM1gUrhpTixLbthI69sy567OnoUyZUb1x0NVM8jWSspSNJtqE01Cj0S+FXzbSCA2381e+75fO5Xs1gVdwTeG6jAUikNRAMV4JeXHQ1UzF8ehS4f/MT8hRf6KVrKdNGi6GV3BDaZJADZ1Hs2u+Cs6FHv2flvBku31Szwvi9Tx5vB/hrZB/6MDT8aODa/cEl06W0B0Sm2BY5k0eJb37jL2zy/8sLolpqzvM3mBuAp4A0gZNZifxh4EQhi7217p7ftDS/5+p7Xv/P6U6+/+Hr4sp8mf3rjT+/6aci8dtlryddufC30Wu0l6xahuf/JrAL2AUeAUCA+f+XmdaU1ywNxoU/YXxsQJeBI4NfwuyXwYfMp4BtAMPAhuRUd8KH5pcujH6w5L/AhEUcCMewv5u23Yf8jIBD49cD7Zf4FrkIa4nC8XwbqjsB7AlewN8sDV6A3F2Fcr0CFrkDJV6DkK1DSFSYcqAq8D3LZT24LvA+9tDVwefbi0lU1lwYuRxl3cX8F9h8EPgXcABwGwmaPPfoB8BMARBz7tUAauJ5nfhIow/Np7D8FHAGCZlswbccybccyncVYHg1ePX88gEquC6xGJVcLKwucD6zEKJ0PrDTNSJuRJpEmkV6P9HqkC83L3g9Qzle8Z4znPeM9nV2+5iu3eU/jx594hyCjTh/3ZjkPsMdQz85HFqHcwG3zC5bJTLiVM+HotvdiKmx7af07oy99KVBqvu9tu3fZ8ujX94VKb/ia97V9wdIb9np7vxou/aoc7vH2IDH7lu1L7rt+X6jmPYHXA69yhH6KFIs18BpSWXA/semrgReZvh74By7e9wXfI/cHK5HK7/cixfVg1Ka/ghSjGqywablNrwi+B00K1LwjeEFwNe9cFVzNHM4OnkUysRypnF9m06X2/JLgWSAXgZoLA4cD32Rdvhk4xJlzKPAn/P0ngW8wvRmpnD9o0z+26YHAN+ZRtqk5MzBulgFrgcuAbUAzUBKYmJ8JlZqabYHPmmogYJYFP2wuA5JAEHPkQvM5YB8QxF7sV+dgXwekgc8BIe957++F5AR/PfghtqwZqbTgKps22bTRph8MxtjSD9jfDUFRkwNHvWPZHaHSo97R7Iwkx7NjASR3ZMcluS07EkZya3Y4XFqz0JvyRjGTSr3f925g+rveBCTxa27zJjCPJrxhZHjNcU8Ei2rZYx51ZVethoLodXoZWWZexuuQ2npXee+DPrjmDk8W6zbv/Xi+Kjt++RohM5XZ1euj/sHyc3jwK9na9/Ogwh28Zx4H2+4M3IIHN3kbpUXeJd5G1GbbUW/j/OVbxbi5MXvhepC7jdvOx2S959uB0vvRxC8C277wrndHvzATLD36xl3zO9KfjDL96Mf89EMfkfS/7qj5QHTHzEK5Z9ulM+Xvic58ySv9/JfCpXNfDpduu2n1mui2L2N3E858GfgDYBbYCcgjK7906Zboti9dehl2a9djh7ZcNeNd9WUPnO0Pg3MchJuQyqDsCs5xwi4Jfik4w+H8IlK58gWb7gjOyHDdEXjJrpF/DLyI1oJxvJhdBx18feAFLBq5sD/wVckh8HWk8nufTb+GFBMmsNeme2z6FXv/7sBXZeIix69mK6LRmguDlwffzeVXhlTqdBlSqcsWm15q081IZSqW2vRdwXdLa25940UcnCXs//zgSt55XnClz41Xzr/jwmigZnlwQfAM9kQEqdxRYtOwPR8KnsFpGhibH1+IwQ18Qvhvz/FA2kwDh4FgMJm9A7M1eI2fxOfvECOF90T2okuEcXp/Nb90RfTiO72/Mi3AD4Cg91BgAyj6xTUXBDZgUW3AMtvApXcxmcd6sPv15DprwYfXitkY+4uBzwFB7/HAOtquvO/NL1wcXVSzzHtYuJP3gEkDAfO495dgFMZ73VSYNd6PvL/DZL/hdu/vzI1AAD+xumrWB38tWM8Oqw9eyca+36Z1SKUTapFKx9fYdJtNq236q8Ersx6mzUJv1KPB0LsBqQhed3mfya67mEvlM9mzz43e6u3wxJR4F+69EVW9Xvbef/GGpRxveH48XHrlUa8ve9k6JL/lJ72S3O5dD4l33RtPeb3z55wbNXd4vWYZAOnfuz57luTc730ctcDCT3Hhp7jwPz6PhY81mJy/pDR6Tc0SL0nug72XBg2QUj9maUFHdpzU5de9uAhT3q967zNpEce992Ybm9mG92ZrrrQHZZfz4H3ZD33YHlz5Af9g/l1lUmJt9rzzeKImG32vPdhUag9WvsMeQJSSg+psdbU9iFbaAxAP/2DzFnuwdr09kJ6Ug/kFC6PbjgeyaM167yIZQ++i+fGS0jtvC+4XDSb4teyZZ1JQ/ZqoMsmaLcG95nrgBuBGYA9wGLgL+A4QAQ/4Op77OvjA182dwD8AbwAluLIPeS4Lfk3yxfWvQT74GjjASm+1+QvpJ5R2eTkrtmr+8orojZiOwjOMtwpDtQrS2SrMu1Xo8xexl8FZlb3gYnv/WedDG/8VeydUSO9sHJ0N+e5sPHO22QMcBu4CzgChPts0A0ng+rfcdQZG93zzTeBOIGiuwv4aoAcYAaaBN4AS5HL+/KYtMlTnZy9/H+uxMNvcbA8urwPJXjg/sbB0Wc1SbwHbIfu13hnY3+mVYL/GC6PnQ9kRUGcvsO2D48HSnzwdLP3KP3nfGWle8038fBoL7I1ve392Ilj61AnvGZz54bhX+udIt92x7fi2O4N33rGw9DhwB6jK1OTC0u3A5PgZ5Ag3VNeRE9yAXpX0syCHTK+sl3Tb9Z+95NLoZ0dDpaOowA3AfwGGgW0jH/5IdAS5/C6Kn8B8+Nx4qHRM6NY4JtUN496qihXnv2fFivIVy69YsXTrisWXr1hQtqLkshXBLSvMpSveuXHJJRuXvqt0ybtLl66/aMnFFy29cM2StWuWLq0503sKjRYnShD7Fd527/fMxVwivzd/7srotpp340QSuAG4ETgMhL1272qzxGvxPiIWssCd6DnZr8D+ODJZ5p2F81u8ZRirZRirZejdZZhXS7ylcr8n15YcCQRfXRE87r2EB170/hGnX/D+4VtLtp29ye+TZZs2sU+2hDZtji5ddtbixWcuWbxg4aLFJWdEFgdD/7Muq4mNm4jCM2tnt2mSdrNNwjZWUsdOtmkdZaEVKZYQSby7PbCqtKQ92PSSSD1QLiB5ljMcWiKhIqSiXOgFidsKaWz34G1BcODAkUO5cyoXJHKGC++9mf0TqrX2e/P8ZubNN2/em1lPzUIcnP1ondvOb05hz+k4Pzt/OKfOFNapOZATa8Zlrwj37nl+3vjbKFh8Za5aWp5bLL82VzEX5jrXuay0WftOIC9woLcDed1r54Z9IK95bXmmczdMOP8yAqksHOcQhaV5nBeAVBrv3w1zfhFfP7BgB8uZbB8+eBQlBRZIfizd2yGSvfdCaR/nZXYnTAo8sKT5KIoieaPdCVEz8lbkvTaofroSyWvIfLUSMQ+uOMaHwOfoij2SegOCV7JZa8mrrSO51TpsjivzybqjS4y3BB3FsVBy6A4kotuFQpekUOy+ohV6LYZmQDUijbAPy+kz/PQGqbiROev+wz6kFLQGcLJBVKETeSO1bd/zogm7YjQALYp1u7Fu0Shlb97AaqWsdkXRxWX/mz5kchqnpXQqS/7vJBO64UZo7deMK4ZDm41NTS8bG5TjappuaLmr6bqma5raml4ynISPYRANRlw2trL6G345BwojJgrDRJqemfZRzXrGPsc9jxiNuBH+AMe3bzEAwf5os+7j/iizXKL4qQeCPDDleWqgi2NEvS2lt3pJ613wvQkcySRID4/T7bqvGHvN1xg9TiuL/tBwePmEvnchk9nriOyTdLGKLVr7ZfY17CG/g/sp7Snx+QvcL6hkgybkLJh4bI0J8QpH0Zf2I/JrdJyReiP80XjXoPM1mHErXV0je26l3rZiMjhNP3wOGid4HqEGrP1pVoRTB6eaBV0TpkDVpO0JMgsXFXYjeLR3NcKfAPse/QnToxnoZQ4h3xsg30PkkUnSJctXPm5ROVsm7HvZ1S1F1Vz09FyMJgMhPoVjH6VQZNwNn1zhBZwJR66g1HbUgJFZtYH5C5iqpSXzC8qLcFg7aV1N2Q5uzxUzX/2fF+DqEbisxGDNI+4xITBAQsCyHa41oVYfirjCSS9wikPDcDIelGJPBQ8We3xMSKJJU3gs+sZTI2l9kBvft+4fNYnkRtq6L/e+OJJ7h7mRuU3oklEg8HhungNl85zbnIxCEw1Dx9A0PiF88Rg4ZPDHwVAd2XQ0ZVREIacAI5hHQxRonNc3T8wT7HC79eER9g02YnOgLDR0AvqOvQlHj8et4VpA6l2F6eg9AYt9i775r3mKXb0kIJDk5p8jIMx/3CYjFMUQdqYmEU1nXT1N2puF7lnjMUQchx2T0XrutRkeo0GPzyICotUm0gYbAM/VQLsAGiqIgZxAFxRowflkVb4OWRTE3XiAPPU1LBFJpjG9dg4CyLEHlGflsguFX6GwA4VZLNw7kFMuJWSQh0mRB0mJBclZoDMssHjC2FI5uck+TtjNd3LzWYvl5vOWnPHkWag24wZsd7fqld/mn9TfqhZnZRGkJTeIGGP/ASqoUjcKZW5kc3RyZWFtCmVuZG9iago2IDAgb2JqCjw8L0Rlc2NlbnQgLTIwOS9DYXBIZWlnaHQgNjk5L1N0ZW1WIDgwL1R5cGUvRm9udERlc2NyaXB0b3IvRm9udEZpbGUyIDUgMCBSL0ZsYWdzIDMyL0ZvbnRCQm94Wy0xMDExIC0zMjkgMjI2MCAxMDc4XS9Gb250TmFtZS9ZQUxOWFIrYXJpYWx1bmkvSXRhbGljQW5nbGUgMC9Bc2NlbnQgNzI4Pj4KZW5kb2JqCjcgMCBvYmoKPDwvRFcgMTAwMC9TdWJ0eXBlL0NJREZvbnRUeXBlMi9DSURTeXN0ZW1JbmZvPDwvU3VwcGxlbWVudCAwL1JlZ2lzdHJ5KEFkb2JlKS9PcmRlcmluZyhJZGVudGl0eSk+Pi9UeXBlL0ZvbnQvQmFzZUZvbnQvWUFMTlhSK2FyaWFsdW5pL0ZvbnREZXNjcmlwdG9yIDYgMCBSL1cgWzNbMjc3XTE5WzU1NiA1NTYgNTU2IDU1NiA1NTYgNTU2IDU1NiA1NTYgNTU2IDU1NiAyNzddMzZbNjY2IDY2NiA3MjIgNzIyXTQzWzcyMl00Nls2NjZdNDhbODMzXTUxWzY2Nl01NFs2NjYgNjEwXTYwWzY2Nl02OFs1NTYgNTU2IDUwMCA1NTYgNTU2IDI3NyA1NTYgNTU2IDIyMl03OVsyMjIgODMzIDU1NiA1NTYgNTU2XTg1WzMzMyA1MDAgMjc3IDU1Nl05MFs3MjJdOTJbNTAwXV0vQ0lEVG9HSURNYXAvSWRlbnRpdHk+PgplbmRvYmoKOCAwIG9iago8PC9GaWx0ZXIvRmxhdGVEZWNvZGUvTGVuZ3RoIDQ5OD4+c3RyZWFtCnicXZTLytswEIX3fgovW7qwLY0kB8JsWgpZ9EKTlm59kYOhsY3jLPL2leek80MN+UDHlnTOKJri4+nTaRq3vPi+zt05bvkwTv0a7/Nj7WLexus4ZZXJ+7HbXiNhd2uWrEiTz8/7Fm+naZiz4zEvfqSX92195u8ul98fyvdZ8W3t4zpO16SQ+fkrKefHsvyJtzhteZkx530c0lJfmuVrc4t5IRPfxMtzibmRcQUH3dzH+9J0cW2ma8yOZXr4+Dk9nMWp/+811ZjVDm+fW1aaknepkgFoXxKx0laQHCutgeRZaS2kwEpLkGpWWgfpwErrITWstAFSy0pbQ+pYaQ+QelbaRiQjvkGCeyO+QYJ7I75BgnsjvkGCeyPbgwQTJrKSWpFS2ZTUQ7KsdKiq9ax02NEGVjrsaJFO6JCRiJUegQhRhB6BCFGEHssTogg9lqealR7HQXIQoMdxkBwE6HEc1LLSoxIkJkH/sjqw0nciuZKVHsVxFSt9hGRY6QdIlpUBJXQSGAyI7VBPYUBsh3oKA2I7CQwGxHaSDgzI6FB1YdgDmbJNA5Ci2d2bKqZ0IA1+92W6cv9K6A5BvurS3+xFd2h3X7berwroXR3kxv67mvvl3fuK9oLusa6pTUjzkWawt4FxitqflnnZZ+Xpl/0FyAopowplbmRzdHJlYW0KZW5kb2JqCjIgMCBvYmoKPDwvU3VidHlwZS9UeXBlMC9UeXBlL0ZvbnQvQmFzZUZvbnQvWUFMTlhSK2FyaWFsdW5pL0VuY29kaW5nL0lkZW50aXR5LUgvRGVzY2VuZGFudEZvbnRzWzcgMCBSXS9Ub1VuaWNvZGUgOCAwIFI+PgplbmRvYmoKNCAwIG9iago8PC9LaWRzWzEgMCBSXS9UeXBlL1BhZ2VzL0NvdW50IDEvSVRYVCgyLjEuNyk+PgplbmRvYmoKOSAwIG9iago8PC9UeXBlL0NhdGFsb2cvUGFnZXMgNCAwIFI+PgplbmRvYmoKMTAgMCBvYmoKPDwvTW9kRGF0ZShEOjIwMTgwOTI3MTU1MjEyKzA4JzAwJykvQ3JlYXRpb25EYXRlKEQ6MjAxODA5MjcxNTUyMTIrMDgnMDAnKS9Qcm9kdWNlcihpVGV4dCAyLjEuNyBieSAxVDNYVCkvVGl0bGUoUERGIHYxKT4+CmVuZG9iagp4cmVmCjAgMTEKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwNzY0IDAwMDAwIG4gCjAwMDAwNzI4MzggMDAwMDAgbiAKMDAwMDAwMDAxNSAwMDAwMCBuIAowMDAwMDcyOTY2IDAwMDAwIG4gCjAwMDAwMDA5MjcgMDAwMDAgbiAKMDAwMDA3MTY4MSAwMDAwMCBuIAowMDAwMDcxODYzIDAwMDAwIG4gCjAwMDAwNzIyNzMgMDAwMDAgbiAKMDAwMDA3MzAyOSAwMDAwMCBuIAowMDAwMDczMDc0IDAwMDAwIG4gCnRyYWlsZXIKPDwvSW5mbyAxMCAwIFIvSUQgWzxlNGNiNzkxNDJjYWEzNjE1ZGE3OTRjZDgzMWNmM2Q3Yj48NmE3MDAxNzNiN2U1MzkzOGYwMTU1N2E5NWIwZTI2NjI+XS9Sb290IDkgMCBSL1NpemUgMTE+PgpzdGFydHhyZWYKNzMyMTEKJSVFT0YK";
//		FileTools.base64ToFile(classPath + "/pdf", base64, "Notification2.pdf");
//	}
	
	@Test
	public void testException() throws Exception{
		for(int i=0; i<10; i++) {
			try {
				this.getException();
			} catch (Exception ee) {
				if("getException".equals(ee.getMessage())) {
					throw new Exception("inner Exception");
				}
				throw ee;
			}
		}
	}
	
	public void getException() throws Exception {
		throw new Exception("getException");
	}

}
