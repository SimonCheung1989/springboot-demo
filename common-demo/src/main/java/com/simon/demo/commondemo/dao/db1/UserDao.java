package com.simon.demo.commondemo.dao.db1;

import com.simon.demo.commondemo.entities.db1.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<UserEntity, Integer> {
}
