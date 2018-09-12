package com.simon.wx.soccer.dependency;

import com.simon.wx.soccer.model.liansai.SaiChengDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${football.url}", name = "footballApi")
public interface FootballApi {

    @RequestMapping(method = RequestMethod.GET, value = "/league")
    SaiChengDTO getSaiCheng(@RequestParam("dtype") String dtype, @RequestParam("league") String league, @RequestParam("key")  String key);
}
