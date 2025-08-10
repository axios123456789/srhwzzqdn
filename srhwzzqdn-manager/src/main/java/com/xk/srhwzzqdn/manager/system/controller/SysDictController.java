package com.xk.srhwzzqdn.manager.system.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.system.service.SysDictService;
import com.xk.srhwzzqdn.model.dto.system.SysDictDto;
import com.xk.srhwzzqdn.model.entity.system.SysAdministrative;
import com.xk.srhwzzqdn.model.entity.system.SysCode;
import com.xk.srhwzzqdn.model.entity.system.SysDict;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统管理-数据字典
 */
@RestController
@RequestMapping("/superBrain/system/dict")
public class SysDictController {
    @Autowired
    private SysDictService sysDictService;

    /**
     * 通过类型获取下拉列表等键和值
     * @param type
     * @return
     */
    @GetMapping("/getKeyAndValueByType/{type}")
    public Result getKeyAndValueByType(@PathVariable("type") String type){
        List<Map<String, Object>> list = sysDictService.getKeyAndValueByType(type);
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据当前账号的权限查询对应的账号等级
     * @param type
     * @return
     */
    @GetMapping("/getUserLevelByPower/{type}")
    public Result getUserLevelByPower(@PathVariable("type") String type){
        List<Map<String, Object>> list = sysDictService.getUserLevelByPower(type);
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }

    /**
     * 条件分页查询数据字典
     * @param current
     * @param limit
     * @param sysDictDto
     * @return
     */
    @GetMapping("/getSysDictList/{current}/{limit}")
    public Result getSysDictList(@PathVariable("current") Integer current,
                                 @PathVariable("limit") Integer limit,
                                 SysDictDto sysDictDto){
        PageInfo<SysDict> pageInfo = sysDictService.getSysDictList(current, limit, sysDictDto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 添加或修改数据字典
     * @param sysDict
     * @return
     */
    @PostMapping("/saveDict")
    public Result saveDict(@RequestBody SysDict sysDict){
        try {
            sysDictService.saveDict(sysDict);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存失败！");
        }
    }

    /**
     * 根据id删除数据字典
     * @param id
     * @return
     */
    @DeleteMapping("/deleteDictById/{id}")
    public Result deleteDictById(@PathVariable("id") String id){
        try {
            sysDictService.deleteDictById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除字典数据失败！");
        }
    }

    /**
     * 获取中国行政区划码值对
     * @return
     */
    @GetMapping("/getAdministrative")
    public Result getAdministrative(){
        List<SysAdministrative> administrativeList = sysDictService.getAdministrative();
        return Result.build(administrativeList, ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据type获取t_sys_code键值对（仅只有一级数据的时候）
     * @param type
     * @return
     */
    @GetMapping("/getSysCodeByType/{type}")
    public Result getSysCodeByType(@PathVariable("type") String type){
        List<SysCode> sysCodes = sysDictService.getSysCodeByType(type);
        return Result.build(sysCodes, ResultCodeEnum.SUCCESS);
    }
}
