package com.simon.wx.soccer.api;

import com.simon.wx.soccer.dependency.FootballApi;
import com.simon.wx.soccer.model.liansai.SaiChengDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SoccerApi {

    @Value(("${football.key}"))
    private String key;

    @Autowired
    private FootballApi footballApi;

    @RequestMapping(method = RequestMethod.GET, value = "/league")
    public SaiChengDTO getSaiCheng(@RequestParam("league") String league) {
        return this.footballApi.getSaiCheng("json", league, key);
    }
}
