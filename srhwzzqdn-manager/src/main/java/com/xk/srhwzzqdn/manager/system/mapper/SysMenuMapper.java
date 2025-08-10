package com.xk.srhwzzqdn.manager.system.mapper;

import com.xk.srhwzzqdn.model.entity.system.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysMenuMapper {
    //获取所有菜单
    List<SysMenu> getAllMenu();

    //添加菜单
    void addMenu(SysMenu sysMenu);

    //根据id查询菜单
    SysMenu getMenuById(String parentId);

    //修改菜单
    void updateMenu(SysMenu sysMenu);

    //根据id查询子菜单的数量
    int getChildrenCountById(String id);

    //根据id删除菜单
    @Update("update t_sys_menu set is_deleted = 1 where id = #{param1}")
    void deleteById(String id);

    //根据用户id查询用户可操作的菜单
    List<SysMenu> findMenusByUserId(String userId);
}
