package com.simon.demo.commondemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {


    public Config(@Value("${appName}") String appName){

        System.out.println("--------");
        System.out.println(appName);
    }


}
