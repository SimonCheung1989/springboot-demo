package com.simon.demo.commondemo.entities.db1;

import javax.persistence.*;

@Entity
@Table(name = "T_USER")
public class UserEntity {
    private Integer id;
    private String name;
//    private Integer score;

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
//
//    @Column(name = "SCORE")
//    public Integer getScore() {
//        return score;
//    }
//
//    public void setScore(Integer score) {
//        this.score = score;
//    }
}
