package com.xk.srhwzzqdn.manager.system.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.system.DistributeMenuDto;
import com.xk.srhwzzqdn.model.dto.system.SysRoleDto;
import com.xk.srhwzzqdn.model.entity.system.SysRole;

import java.util.Map;

public interface SysRoleService {
    //条件分页查询角色列表
    PageInfo<SysRole> findByPage(SysRoleDto sysRoleDto, Integer current, Integer limit);

    //添加或修改角色
    void saveRole(SysRole sysRole);

    //根据id删除角色
    void deleteRoleById(String id);

    //获取所有角色和用户已经分配的角色
    Map<String, Object> getAllRoles(String userId);

    //保存角色分配菜单数据
    void allocateMenus(DistributeMenuDto distributeMenuDto);
}
