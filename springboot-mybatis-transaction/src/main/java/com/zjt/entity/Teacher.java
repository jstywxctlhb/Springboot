package com.zjt.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "t_teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String name;
}