package com.simon.demo.commondemo.dao;

import com.simon.demo.commondemo.entities.BlogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogDao extends JpaRepository<BlogEntity, Integer> {
}
