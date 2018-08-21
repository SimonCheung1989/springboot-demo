package com.simon.demo.loginservice.feign;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public Retryer feignRetryer(){
        return new Retryer.Default();
    }
}
