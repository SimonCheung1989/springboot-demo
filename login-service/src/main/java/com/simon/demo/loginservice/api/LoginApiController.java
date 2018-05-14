package com.simon.demo.loginservice.api;

import com.simon.demo.loginservice.model.LoginDTO;
import com.simon.demo.loginservice.model.ProfileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.List;

@Controller
public class LoginApiController {

    private static final Logger log = LoggerFactory.getLogger(LoginApiController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<ProfileDTO> loginPost(@Valid @RequestBody LoginDTO loginDTO) {
//        ProfileDTO profileDTO = new ProfileDTO();
//
//        profileDTO.setLoginName(loginDTO.getLoginName());
//        profileDTO.setPassword(loginDTO.getPassword());

        ProfileDTO profileDTO = null;
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances("PROFILE-SERVICE");
        if(serviceInstanceList!=null && !serviceInstanceList.isEmpty()) {
            profileDTO = restTemplate.getForObject(serviceInstanceList.get(0).getUri() + loginDTO.getLoginName(), ProfileDTO.class);
        }
//        ProfileDTO profileDTO = restTemplate.getForObject("http://localhost:18090/profile/" + loginDTO.getLoginName(), ProfileDTO.class);

        return new ResponseEntity(profileDTO, HttpStatus.OK);
    }

}
