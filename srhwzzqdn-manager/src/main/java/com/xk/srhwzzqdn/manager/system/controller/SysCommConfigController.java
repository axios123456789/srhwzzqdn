package com.xk.srhwzzqdn.manager.system.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.system.service.SysCommConfigService;
import com.xk.srhwzzqdn.model.dto.system.SysCommConfigQueryDto;
import com.xk.srhwzzqdn.model.entity.system.SysCommConfig;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.model.vo.system.InterfaceCompareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/system/sysCommConfig")
public class SysCommConfigController {

    @Autowired
    private SysCommConfigService sysCommConfigService;

    @GetMapping("/findPage/{current}/{limit}")
    public Result findPage(@PathVariable("current") Integer current,
                           @PathVariable("limit") Integer limit,
                           SysCommConfigQueryDto queryDto) {
        PageInfo<SysCommConfig> pageInfo = sysCommConfigService.findPage(current, limit, queryDto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    @GetMapping("/findAllEnabled")
    public Result findAllEnabled() {
        List<SysCommConfig> list = sysCommConfigService.findAllEnabled();
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }

    @GetMapping("/findById/{id}")
    public Result findById(@PathVariable("id") String id) {
        SysCommConfig config = sysCommConfigService.findById(id);
        return Result.build(config, ResultCodeEnum.SUCCESS);
    }

    @PostMapping("/save")
    public Result save(@RequestBody SysCommConfig config) {
        try {
            sysCommConfigService.save(config);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存接口配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result update(@RequestBody SysCommConfig config) {
        try {
            sysCommConfigService.update(config);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "更新接口配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable("id") String id) {
        try {
            sysCommConfigService.deleteById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除接口配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/aiFetchLatest")
    public Result aiFetchLatest() {
        try {
            List<InterfaceCompareVo> result = sysCommConfigService.aiFetchLatest();
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "AI获取最新接口失败: " + e.getMessage());
        }
    }
}