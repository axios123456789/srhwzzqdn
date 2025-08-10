package com.xk.srhwzzqdn.manager.system.controller;

import com.xk.srhwzzqdn.manager.system.mapper.SysPersonInfoMapper;
import com.xk.srhwzzqdn.manager.system.service.SysPersonInfoService;
import com.xk.srhwzzqdn.model.entity.system.SysPersonInfo;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/superBrain/system/sysPersonInfo")
public class SysPersonInfoController {
    @Autowired
    private SysPersonInfoService sysPersonInfoService;

    @Autowired
    private SysPersonInfoMapper sysPersonInfoMapper;

    /**
     * 获取个人信息
     * @return
     */
    @GetMapping("/getPersonInfo")
    public Result getPersonInfo(){
        SysPersonInfo sysPersonInfo = sysPersonInfoService.getPersonInfo();
        return Result.build(sysPersonInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存个人信息
     * @param sysPersonInfo
     * @return
     */
    @PostMapping("/savePersonInfo/{codeBlock}")
    public Result savePersonInfo(@PathVariable("codeBlock") String codeBlock, @RequestBody SysPersonInfo sysPersonInfo){
        try {
            /*//1.获取个人信息
            SysPersonInfo getSysPersonInfo = sysPersonInfoMapper.getPersonInfo(AuthContextUtil.get().getId());
            if (getSysPersonInfo == null){
                //插入一条个人信息记录
                SysPersonInfo sysPersonInfo1 = new SysPersonInfo();
                sysPersonInfo1.setId(UUIDUtil.getUUID());
                sysPersonInfo1.setAccountId(AuthContextUtil.get().getId());
                sysPersonInfo1.setCreateBy(AuthContextUtil.get().getUserName());
                sysPersonInfoMapper.addPersonInfo(sysPersonInfo1);
            }*/
            sysPersonInfoService.savePersonInfo(codeBlock, sysPersonInfo);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存主人信息失败！");
        }
    }

    /**
     * 根据id删除校园级别块
     * @param id
     * @return
     */
    @DeleteMapping("/deletePersonSchoolInfoById/{id}")
    public Result deletePersonSchoolInfoById(@PathVariable("id") String id){
        try {
            sysPersonInfoService.deletePersonSchoolInfoById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "移除失败，请联系相关人员！");
        }
    }
}
