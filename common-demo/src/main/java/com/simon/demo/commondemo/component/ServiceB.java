package com.simon.demo.commondemo.component;

import com.simon.demo.commondemo.dao.db1.UserDao;
import com.simon.demo.commondemo.dao.db2.BlogDao;
import com.simon.demo.commondemo.entities.db1.UserEntity;
import com.simon.demo.commondemo.entities.db2.BlogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class ServiceB {

    @Autowired
    UserDao userDao;

    @Autowired
    BlogDao blogDao;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void insertUserEntity(UserEntity userEntity){
        this.userDao.save(userEntity);
    }

}
