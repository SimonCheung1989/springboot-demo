package com.simon.demo.commondemo.entities.db2;

import javax.persistence.*;

@Entity
@Table(name = "T_USER")
public class UserEntity {
    private Integer id;
    private String name;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
