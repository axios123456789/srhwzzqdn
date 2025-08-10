package com.xk.srhwzzqdn.manager.system.mapper;

import com.xk.srhwzzqdn.model.dto.system.SysUserDto;
import com.xk.srhwzzqdn.model.entity.system.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysUserMapper {
    //根据账号查询查询系统用户
    SysUser getSysUserByAccount(String loginAccount);

    //条件分页查询系统用户
    List<SysUser> findByPage(SysUserDto sysUserDto);

    //添加用户
    void addUser(SysUser sysUser);

    //修改用户
    void updateUser(SysUser sysUser);

    //根据id删除用户
    @Update("update t_sys_user set is_deleted = 1 where id = #{param1}")
    void deleteUserById(String id);

    //获取主脑账号数
    @Select("select count(1) from t_sys_user t where t.`level` = 1 and t.is_deleted = 0")
    int getMainBrainCount();

    @Update("update t_sys_user set password = #{param1} where id = #{param2}")
    void updatePassword(String md5DigestAsHex, String id);
}
