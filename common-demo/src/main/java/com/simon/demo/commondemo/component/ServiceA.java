package com.simon.demo.commondemo.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceA {

    @Value("${appName}")
    private String appName;

    public String getAppName(){
        return appName;
    }
}
