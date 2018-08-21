package com.simon.demo.loginservice.api;

import com.simon.demo.loginservice.feign.ProfileClient;
import com.simon.demo.loginservice.model.LoginDTO;
import com.simon.demo.loginservice.model.ProfileDTO;
import com.simon.demo.loginservice.svc.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

@Controller
public class LoginApiController {

    private static final Logger log = LoggerFactory.getLogger(LoginApiController.class);

    @Autowired
    private LoginService loginService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<ProfileDTO> loginPost(@Valid @RequestBody LoginDTO loginDTO) throws Exception {
        ProfileDTO profileDTO = loginService.login(loginDTO);

        return new ResponseEntity(profileDTO, HttpStatus.OK);
    }


    @Autowired
    private ProfileClient profileClient;

    @RequestMapping(value = "/login1", method = RequestMethod.POST)
    public ResponseEntity<ProfileDTO> login1Post(@Valid @RequestBody LoginDTO loginDTO) throws Exception {
        ProfileDTO profileDTO = null;
        profileDTO = profileClient.getProfile(loginDTO.getLoginName());

        return new ResponseEntity(profileDTO, HttpStatus.OK);
    }


}
