package com.simon.demo.commondemo.component;

import com.simon.demo.commondemo.dao.db1.UserDao;
import com.simon.demo.commondemo.dao.db2.BlogDao;
import com.simon.demo.commondemo.entities.db1.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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

    @Async
    public void sendNotification(Integer number){
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Success: " + number);
    }
}
