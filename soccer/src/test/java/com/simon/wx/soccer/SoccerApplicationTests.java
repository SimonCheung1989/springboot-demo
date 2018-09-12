package com.simon.wx.soccer;

import com.simon.wx.soccer.dependency.FootballApi;
import com.simon.wx.soccer.model.liansai.SaiChengDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SoccerApplicationTests {

	@Value("${football.url}")
	private String url;

	@Value(("${football.key}"))
	private String key;

	@Test
	public void contextLoads() {
	}

	@Autowired
	private FootballApi footballApi;

	@Test
	public void testGetSaiCheng() {
		System.out.println(url);
		System.out.println(key);

		SaiChengDTO saiChengDTO = footballApi.getSaiCheng("json", "法甲", key);
		System.out.println(saiChengDTO.getReason());

	}

}
