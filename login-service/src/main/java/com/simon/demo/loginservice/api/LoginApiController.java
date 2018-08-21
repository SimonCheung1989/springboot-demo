package com.simon.demo.loginservice.api;

import com.simon.demo.loginservice.model.LoginDTO;
import com.simon.demo.loginservice.model.ProfileDTO;
import com.simon.demo.loginservice.svc.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.net.ConnectException;

@Controller
public class LoginApiController {

    private static final Logger log = LoggerFactory.getLogger(LoginApiController.class);

    @Autowired
    private LoginService loginService;

//    @Autowired
//    private DiscoveryClient discoveryClient;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<ProfileDTO> loginPost(@Valid @RequestBody LoginDTO loginDTO) throws Exception {
//        ProfileDTO profileDTO = new ProfileDTO();
//
//        profileDTO.setLoginName(loginDTO.getLoginName());
//        profileDTO.setPassword(loginDTO.getPassword());

//        ProfileDTO profileDTO = null;
//        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances("PROFILE-SERVICE");
//        if(serviceInstanceList!=null && !serviceInstanceList.isEmpty()) {
//            profileDTO = restTemplate.getForObject(serviceInstanceList.get(0).getUri() + loginDTO.getLoginName(), ProfileDTO.class);
//        }
        ProfileDTO profileDTO = loginService.login(loginDTO);

        return new ResponseEntity(profileDTO, HttpStatus.OK);
    }



}
