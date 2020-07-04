package com.zjt.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "t_class")
public class TClass {

    @Id
    private String id;


    private String name;
}