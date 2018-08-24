package com.simon.demo.commondemo.dao.db1;

import com.simon.demo.commondemo.entities.db1.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao extends JpaRepository<UserEntity, Integer> {

    @Query(value = "select * from T_USER where NAME like :name% order by NAME limit :offset, :limit",
            nativeQuery = true)
    List<UserEntity> findByName(@Param("name") String name, @Param("offset") Integer offset, @Param("limit") Integer limit);
}
