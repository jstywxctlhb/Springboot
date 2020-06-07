package com.wang.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wang.model.entity.User;
import lombok.Data;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

@Data
@Table(name = "user")
public class UserDto extends User {

    @Transient
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date loginTime;
}
