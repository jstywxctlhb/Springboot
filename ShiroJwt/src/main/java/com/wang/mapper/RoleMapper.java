package com.wang.mapper;

import com.wang.model.RoleDto;
import com.wang.model.UserDto;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * RoleMapper
 */
public interface RoleMapper extends Mapper<RoleDto> {

    /**
     * 根据User查询Role
     */
    List<RoleDto> findRoleByUser(UserDto userDto);
}