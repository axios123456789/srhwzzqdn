package com.xk.srhwzzqdn.manager.system.mapper;

import com.xk.srhwzzqdn.model.entity.system.SysPersonInfo;
import com.xk.srhwzzqdn.model.entity.system.SysPersonSchoolInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysPersonInfoMapper {
    //根据account_id获取个人信息
    SysPersonInfo getPersonInfo(String id);

    //根据person_info_id获取个人学历信息
    List<SysPersonSchoolInfo> getPersonSchoolInfos(String id);

    //添加个人信息
    void addPersonInfo(SysPersonInfo sysPersonInfo1);

    //修改个人信息
    void updatePersonInfo(SysPersonInfo sysPersonInfo);

    //添加学校模块信息
    void addPersonSchoolInfo(SysPersonSchoolInfo sysPersonSchoolInfo);

    //修改学校模块信息
    void updatePersonSchoolInfo(SysPersonSchoolInfo sysPersonSchoolInfo);

    //根据id删除校园级别块
    @Delete("delete from t_sys_person_school_info where id = #{param1}")
    void deletePersonSchoolInfoById(String id);
}
