package com.simon.demo.commondemo.dao.db2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.simon.demo.commondemo.entities.db2.UserEntity2;

@Repository
public interface UserDao2 extends JpaRepository<UserEntity2, Integer> {
}
