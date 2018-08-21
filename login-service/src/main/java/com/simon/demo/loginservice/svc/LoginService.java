package com.simon.demo.loginservice.svc;

import com.simon.demo.loginservice.api.LoginApiController;
import com.simon.demo.loginservice.model.LoginDTO;
import com.simon.demo.loginservice.model.ProfileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.Null;
import java.net.ConnectException;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginApiController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Retryable(include={RuntimeException.class, NullPointerException.class, ConnectException.class, RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000l, multiplier = 1))
    public ProfileDTO login(LoginDTO loginDTO) {
        System.out.println("Retry");
        String str = null;
//        System.out.println(str.toString());
//        if(true) {
//            throw new RuntimeException("RPC调用异常");
//        }

        return restTemplate.getForObject("http://localhost:18090/profile-service/profile/" + loginDTO.getLoginName(), ProfileDTO.class);
    }

//    @Recover
//    public ProfileDTO recover(RuntimeException e, LoginDTO loginDTO) {
//        System.out.println("RuntimeException************");
//        return null;
//    }

    @Recover
    public ProfileDTO recover(NullPointerException e, LoginDTO loginDTO) {
        System.out.println("NullPointerException************");
        return null;
    }

    @Recover
    public ProfileDTO recover(RestClientException e, LoginDTO loginDTO) {
        System.out.println("RestClientException************");
        return null;
    }

}
