package com.wang.mapper;

import com.wang.model.PermissionDto;
import com.wang.model.RoleDto;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * PermissionMapper
 */
public interface PermissionMapper extends Mapper<PermissionDto> {

    /**
     * 根据Role查询Permission
     */
    List<PermissionDto> findPermissionByRole(RoleDto roleDto);
}