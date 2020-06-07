package com.wang.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wang.model.valid.group.UserEditValidGroup;
import com.wang.model.valid.group.UserLoginValidGroup;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Table(name = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 3342723124953988435L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 帐号
     */
    @NotNull(message = "帐号不能为空", groups = {UserLoginValidGroup.class, UserEditValidGroup.class})
    private String account;

    /**
     * 密码
     */
    @NotNull(message = "密码不能为空", groups = {UserLoginValidGroup.class, UserEditValidGroup.class})
    private String password;

    /**
     * 昵称
     */
    @NotNull(message = "用户名不能为空", groups = {UserEditValidGroup.class})
    private String username;

    /**
     * 注册时间
     */
    @Column(name = "reg_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date regTime;
}