package com.xk.srhwzzqdn.manager.system.service;

import com.xk.srhwzzqdn.model.entity.system.SysMenu;
import com.xk.srhwzzqdn.model.vo.system.SysMenuVo;

import java.util.List;
import java.util.Map;

public interface SysMenuService {
    //查询菜单列表（以特定形式返回查询结果）
    List<SysMenu> findNodes();

    //添加菜单
    void addMenu(SysMenu sysMenu);

    //获取当前添加菜单的父菜单
    SysMenu selectParentMenu(String parentId);

    //修改菜单
    void updateMenu(SysMenu sysMenu);

    //删除菜单
    void deleteMenuById(String id);

    //查询所有菜单和角色分配过的菜单
    Map<String, Object> findAllMenusWithRoleId(String roleId);

    //查询用户可以操作菜单
    List<SysMenuVo> findMenusByUserId();
}
