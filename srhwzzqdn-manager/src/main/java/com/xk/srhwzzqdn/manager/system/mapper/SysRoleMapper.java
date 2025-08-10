package com.xk.srhwzzqdn.manager.system.mapper;

import com.xk.srhwzzqdn.model.dto.system.SysRoleDto;
import com.xk.srhwzzqdn.model.entity.system.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    //条件查询所有角色
    List<SysRole> findByPage(SysRoleDto sysRoleDto);

    //添加角色
    void addRole(SysRole sysRole);

    //修改角色
    void updateRole(SysRole sysRole);

    //根据id删除角色
    @Update("update t_sys_role set is_deleted = 1 where id = #{param1}")
    void deleteRoleById(String id);

    //查询所有角色
    @Select("select id, role_name as roleName from t_sys_role where is_deleted = 0")
    List<SysRole> getAllRoles();

    //根据角色code或取角色id
    @Select("select id from t_sys_role where role_code = #{param1} and is_deleted = 0")
    String getRoleIdByRoleCode(String mainMaster);
}
