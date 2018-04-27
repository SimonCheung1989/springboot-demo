package com.simon.demo.loginservice.api;

import com.simon.demo.loginservice.model.LoginDTO;
import com.simon.demo.loginservice.model.ProfileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<ProfileDTO> loginPost(@Valid @RequestBody LoginDTO loginDTO) {
        ProfileDTO profileDTO = new ProfileDTO();

        profileDTO.setLoginName(loginDTO.getLoginName());
        profileDTO.setPassword(loginDTO.getPassword());

        return new ResponseEntity(profileDTO, HttpStatus.OK);
    }

}
