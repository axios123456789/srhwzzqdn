package com.xk.srhwzzqdn.manager.system.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.system.*;
import com.xk.srhwzzqdn.model.entity.system.SysUser;
import com.xk.srhwzzqdn.model.vo.system.LoginVo;

public interface SysUserService {
    //用户登录
    LoginVo login(LoginDto loginDto);

    //用户退出登录
    void logout(String token);

    //查询系统用户列表
    PageInfo<SysUser> findByPage(Integer current, Integer limit, SysUserDto sysUserDto);

    //保存用户
    void saveUser(SysUser sysUser);

    //根据id删除用户
    void deleteUserById(String id);

    //用户分配角色
    void distributeRole(DistributeRoleDto distributeRoleDto);

    //判断是否注册了主脑账号
    boolean isRegisterMainBrain();

    //外置大脑账号注册
    void registerBrainAccount(RegisterDto registerDto);

    //修改密码
    void editPassword(EditPasswordDto editPasswordDto, String token);
}
