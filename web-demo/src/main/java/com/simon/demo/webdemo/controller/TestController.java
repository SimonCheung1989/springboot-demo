package com.simon.demo.webdemo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Map test(){
        Map map = new HashMap();

        map.put("name", "Simon");
        System.out.println("------------------");
        return map;
    }
}
