package com.simon.demo.commondemo.dao.db2;

import com.simon.demo.commondemo.entities.db2.BlogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogDao extends JpaRepository<BlogEntity, Integer> {
}
