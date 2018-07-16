package com.simon.demo.profileservice.api;

import com.simon.demo.profileservice.dao.ProfileRepository;
import com.simon.demo.profileservice.entities.TProfileEntity;
import com.simon.demo.profileservice.model.ProfileDTO;
import com.simon.demo.profileservice.util.ConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@Controller
public class ProfileApiController {

    private static final Logger log = LoggerFactory.getLogger(ProfileApiController.class);

    @Autowired
    private ProfileRepository profileRepository;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> test(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("methodName", "test");
        return map;
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ResponseEntity<List<ProfileDTO>> profileGet() {
        List<TProfileEntity> list = this.profileRepository.findAll();
        if(list==null && list.size()==0){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        List<ProfileDTO> result = new ArrayList<ProfileDTO>();

        for (TProfileEntity entity : list) {
            ProfileDTO dto = ConvertUtil.convertJavaBean2DestinationJavaBean(entity, ProfileDTO.class);
            result.add(dto);
        }

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/profile/{id}", method = RequestMethod.GET)
    public ResponseEntity<ProfileDTO> profileIdGet(@PathVariable("id") String id) {
        Optional<TProfileEntity> optional = this.profileRepository.findById(id);
        if(!optional.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        ProfileDTO result = ConvertUtil.convertJavaBean2DestinationJavaBean(optional.get(), ProfileDTO.class);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public ResponseEntity<ProfileDTO> profilePost(@Valid @RequestBody ProfileDTO profileDTO) {
        TProfileEntity entity = ConvertUtil.convertJavaBean2DestinationJavaBean(profileDTO, TProfileEntity.class);
        this.profileRepository.save(entity);
        return new ResponseEntity(profileDTO, HttpStatus.NOT_IMPLEMENTED);
    }

}
