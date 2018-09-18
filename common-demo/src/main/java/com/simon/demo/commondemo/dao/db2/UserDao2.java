package com.simon.demo.commondemo.dao.db2;

import com.simon.demo.commondemo.entities.db2.UserEntity2;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao2 extends JpaRepository<UserEntity2, Integer> {
}
