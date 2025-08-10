package com.xk.srhwzzqdn.manager.system.service.impl;

import com.xk.srhwzzqdn.manager.system.mapper.SysPersonInfoMapper;
import com.xk.srhwzzqdn.manager.system.service.SysPersonInfoService;
import com.xk.srhwzzqdn.model.entity.system.SysPersonInfo;
import com.xk.srhwzzqdn.model.entity.system.SysPersonSchoolInfo;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class SysPersonInfoServiceImpl implements SysPersonInfoService {
    @Autowired
    private SysPersonInfoMapper sysPersonInfoMapper;

    /**
     * 获取个人信息
     *
     * @return
     */
    @Override
    public SysPersonInfo getPersonInfo() {
        //1.获取个人信息
        SysPersonInfo sysPersonInfo = sysPersonInfoMapper.getPersonInfo(AuthContextUtil.get().getId());
        if (sysPersonInfo == null){
            return sysPersonInfo;
        }

        //2.获取个人学历信息列表
        List<SysPersonSchoolInfo> sysPersonSchoolInfoList = sysPersonInfoMapper.getPersonSchoolInfos(sysPersonInfo.getId());
        //3.设置个人学历列表
        sysPersonInfo.setSysPersonSchoolInfoList(sysPersonSchoolInfoList);
        //4.设置地址列表
        if (sysPersonInfo.getHometownAddress() != null && !"".equals(sysPersonInfo.getHometownAddress())){
            sysPersonInfo.setHometownAddressList(Arrays.asList(sysPersonInfo.getHometownAddress().split(",")));
        }
        if (sysPersonInfo.getCurrentAddress() != null && !"".equals(sysPersonInfo.getCurrentAddress())){
            sysPersonInfo.setCurrentAddressList(Arrays.asList(sysPersonInfo.getCurrentAddress().split(",")));
        }
        //设置校园经历模块-技能证书
        if (sysPersonInfo.getCertificates() != null && !"".equals(sysPersonInfo.getCertificates())){
            sysPersonInfo.setCertificateList(Arrays.asList(sysPersonInfo.getCertificates().split(",")));
        }
        //设置其他模块
        //兴趣爱好
        if (sysPersonInfo.getHobbies() != null && !"".equals(sysPersonInfo.getHobbies())){
            sysPersonInfo.setHobbyList(Arrays.asList(sysPersonInfo.getHobbies().split(",")));
        }
        //语言能力
        if (sysPersonInfo.getLanguages() != null && !"".equals(sysPersonInfo.getLanguages())){
            sysPersonInfo.setLanguageList(Arrays.asList(sysPersonInfo.getLanguages().split(",")));
        }

        return sysPersonInfo;
    }

    /**
     * 保存主人信息
     * @param sysPersonInfo
     */
    @Override
    public void savePersonInfo(String codeBlock, SysPersonInfo sysPersonInfo) {
        //修改个人信息
        sysPersonInfo.setCodeBlock(codeBlock);
        if ("contact".equals(codeBlock)){//如果是联系方式区块，需要特殊处理
            if (sysPersonInfo.getHometownAddressList() != null && sysPersonInfo.getHometownAddressList().size() > 0){
                sysPersonInfo.setHometownAddress(String.join(",", sysPersonInfo.getHometownAddressList()));
            }
            if (sysPersonInfo.getCurrentAddressList() != null && sysPersonInfo.getCurrentAddressList().size() > 0){
                sysPersonInfo.setCurrentAddress(String.join(",", sysPersonInfo.getCurrentAddressList()));
            }
        }else if ("education".equals(codeBlock)){//教育背景块也需要特殊处理
            if (sysPersonInfo.getCertificateList() != null && sysPersonInfo.getCertificateList().size() > 0){
                sysPersonInfo.setCertificates(String.join(",", sysPersonInfo.getCertificateList()));
            }
            //修改教育信息
            for (SysPersonSchoolInfo sysPersonSchoolInfo : sysPersonInfo.getSysPersonSchoolInfoList()){
                if (sysPersonSchoolInfo.getId() == null || "".equals(sysPersonSchoolInfo.getId())){//添加
                    sysPersonSchoolInfo.setId(UUIDUtil.getUUID());
                    sysPersonSchoolInfo.setPersonInfoId(sysPersonInfo.getId());
                    sysPersonInfoMapper.addPersonSchoolInfo(sysPersonSchoolInfo);
                }else {//修改
                    sysPersonInfoMapper.updatePersonSchoolInfo(sysPersonSchoolInfo);
                }
            }
        }else if ("other".equals(codeBlock)){//其他模块也需要特殊处理
            if (sysPersonInfo.getHobbyList() != null && sysPersonInfo.getHobbyList().size() > 0){
                sysPersonInfo.setHobbies(String.join(",", sysPersonInfo.getHobbyList()));
            }
            if (sysPersonInfo.getLanguageList() != null && sysPersonInfo.getLanguageList().size() > 0){
                sysPersonInfo.setLanguages(String.join(",", sysPersonInfo.getLanguageList()));
            }
        }
        sysPersonInfoMapper.updatePersonInfo(sysPersonInfo);
    }

    /**
     * 根据id删除校园级别块
     * @param id
     */
    @Override
    public void deletePersonSchoolInfoById(String id) {
        sysPersonInfoMapper.deletePersonSchoolInfoById(id);
    }
}
