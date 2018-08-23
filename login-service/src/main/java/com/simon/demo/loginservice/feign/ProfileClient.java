package com.simon.demo.loginservice.feign;

import com.simon.demo.loginservice.model.ProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "profile-service")
public interface ProfileClient {

    @RequestMapping(method = RequestMethod.GET, value = "/profile-service/profile/{userId}")
    ProfileDTO getProfile(@PathVariable("userId") String userId);

}
