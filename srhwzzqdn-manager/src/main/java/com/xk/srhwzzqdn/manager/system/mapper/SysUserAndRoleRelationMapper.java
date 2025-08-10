package com.xk.srhwzzqdn.manager.system.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserAndRoleRelationMapper {
    //根据用户id获取角色ids
    @Select("select role_id as roleId from t_sys_user_role_relation ur where user_id = #{param1}")
    List<String> getRoleIdsByUserId(String userId);

    //根据userId删除角色
    @Delete("delete from t_sys_user_role_relation where user_id = #{param1}")
    void deleteRoleByUserId(String userId);

    //分配角色
    void addRoleByUserId(List<Map<String, Object>> userAndRoleList);
}
