package com.simon.demo.profileservice.dao;

import com.simon.demo.profileservice.entities.TProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<TProfileEntity, String> {
}
