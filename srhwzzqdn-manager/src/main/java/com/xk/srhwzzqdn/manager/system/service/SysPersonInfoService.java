package com.xk.srhwzzqdn.manager.system.service;

import com.xk.srhwzzqdn.model.entity.system.SysPersonInfo;

public interface SysPersonInfoService {
    //获取个人信息
    SysPersonInfo getPersonInfo();

    //保存主人信息
    void savePersonInfo(String codeBlock, SysPersonInfo sysPersonInfo);

    //根据id删除校园级别块
    void deletePersonSchoolInfoById(String id);
}
