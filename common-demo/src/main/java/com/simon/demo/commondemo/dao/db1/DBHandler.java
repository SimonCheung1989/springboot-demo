package com.simon.demo.commondemo.dao.db1;

import com.simon.demo.commondemo.entities.db1.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class DBHandler {
    @PersistenceContext
    EntityManager entityManager;

    public List<UserEntity> queryUserEntity(String name, int sortValue){
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = builder.createQuery(UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);
        query = query.select(root);
        if(!StringUtils.isEmpty(name)) {
            query.where(builder.like(root.get("name"), name + "%" ));
            if(sortValue == 1) {
                query.orderBy(builder.asc(root.get("name")));
            } else if(sortValue == -1) {
                query.orderBy(builder.desc(root.get("name")));
            }
        }
        return entityManager.createQuery(query).getResultList();

    }
}
