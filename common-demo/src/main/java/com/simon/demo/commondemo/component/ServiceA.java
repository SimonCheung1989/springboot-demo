package com.simon.demo.commondemo.component;

import com.simon.demo.commondemo.dao.db1.UserDao;
import com.simon.demo.commondemo.dao.db2.BlogDao;
import com.simon.demo.commondemo.entities.db1.UserEntity;
import com.simon.demo.commondemo.entities.db2.BlogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
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

    public void transfer(UserEntity user1, UserEntity user2) {
//        UserEntity user1 = this.userDao.getOne(1);
//        UserEntity user2 = this.userDao.getOne(2);

        UserEntity firstUser = user1.getId() < user2.getId() ? user1: user2;
        UserEntity secondUser = user1.getId() > user2.getId() ? user1: user2;

//        UserEntity firstUser = user1;
//        UserEntity secondUser = user2;

//        synchronized (firstUser) {
//            System.out.println("sync " + firstUser.getId());
//            synchronized (secondUser) {
//                System.out.println("sync " + secondUser.getId());
//                user1.setScore(user1.getScore() + 1);
//                user2.setScore(user2.getScore() - 1);
//            }
//        }


//        this.userDao.save(user1);
//        this.userDao.save(user2);
    }

    public void saveAndSendNotification(Integer number){
        System.out.println("Saving: " + number);
        serviceB.sendNotification(number);
        System.out.println("Saved: " + number);
    }


}
