package com.wang.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Table(name = "role")
public class Role implements Serializable {

    private static final long serialVersionUID = 6382925944937625109L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 角色名称
     */
    private String name;
}