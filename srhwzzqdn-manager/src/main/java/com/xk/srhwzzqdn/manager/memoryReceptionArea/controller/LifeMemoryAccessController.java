package com.xk.srhwzzqdn.manager.memoryReceptionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.LifeMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LifeMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LifeMemory;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/memoryReception/lifeMemoryAccess")
public class LifeMemoryAccessController {
    @Autowired
    private LifeMemoryAccessService lifeMemoryAccessService;

    /**
     * 条件分页查询生活记忆列表
     * @param current
     * @param limit
     * @param lifeMemoryDto
     * @return
     */
    @RequestMapping("/getLifeMemoryByConditionAndPage/{current}/{limit}")
    public Result getLifeMemoryByConditionAndPage(@PathVariable("current") Integer current,
                                                  @PathVariable("limit") Integer limit,
                                                  @RequestBody LifeMemoryDto lifeMemoryDto){
        PageInfo<LifeMemory> lifeMemoryPageInfo = lifeMemoryAccessService.getLifeMemoryByConditionAndPage(current, limit, lifeMemoryDto);
        return Result.build(lifeMemoryPageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存生活记忆
     * @param lifeMemory
     * @return
     */
    @PostMapping("/saveLifeMemory")
    public Result saveLifeMemory(@RequestBody LifeMemory lifeMemory){
        try {
            lifeMemoryAccessService.saveLifeMemory(lifeMemory);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存生活记忆失败！");
        }
    }

    /**
     * 根据id删除生活记忆
     * @param id
     * @return
     */
    @DeleteMapping("/deleteLifeMemoryById/{id}")
    public Result deleteLifeMemoryById(@PathVariable("id") Integer id){
        try {
            lifeMemoryAccessService.deleteLifeMemoryById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除生活记忆失败！");
        }
    }

    /**
     * 根据ids批量删除生活记忆
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllLifeMemoryByIds")
    public Result deleteAllLifeMemoryByIds(@RequestBody List<Integer> ids){
        try {
            lifeMemoryAccessService.deleteAllLifeMemoryByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除生活记忆失败！");
        }
    }
}
