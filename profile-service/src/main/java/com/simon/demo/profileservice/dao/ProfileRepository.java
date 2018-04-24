package com.simon.demo.profileservice.dao;

import com.simon.demo.profileservice.entities.TProfileEntity;
import org.springframework.data.repository.*;


public interface ProfileRepository extends Repository<TProfileEntity, String> {
}
