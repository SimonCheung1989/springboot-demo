package com.simon.demo.commondemo.component;

import com.simon.demo.commondemo.dao.db1.UserDao;
import com.simon.demo.commondemo.dao.db2.BlogDao;
import com.simon.demo.commondemo.entities.db1.UserEntity;
import com.simon.demo.commondemo.entities.db2.BlogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.transaction.Transactional;

@Component
public class ServiceA {

    @Value("${appName}")
    private String appName;

    public String getAppName(){
        return appName;
    }

    @Autowired
    UserDao userDao;

    @Autowired
    BlogDao blogDao;

    @Autowired
    ServiceB serviceB;

    @Transactional
    public void insert(UserEntity userEntity, BlogEntity blogEntity){
//        this.userDao.save(userEntity);
        serviceB.insertUserEntity(userEntity);
        this.blogDao.save(blogEntity);
        throw new NullPointerException();

    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void insertUserEntity(UserEntity userEntity){
        this.userDao.save(userEntity);
    }

    private final TransactionTemplate transactionTemplate;

    ServiceA(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    //    @Transactional
    public void insertWithManualTransactional(UserEntity userEntity, BlogEntity blogEntity){
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                serviceB.insertUserEntity(userEntity);
                blogDao.save(blogEntity);
                System.out.println(1/0);
            }
        });

    }
}
