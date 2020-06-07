package com.wang.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Table(name = "permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = -8834983208597107688L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 权限代码字符串
     */
    @Column(name = "per_code")
    private String perCode;
}