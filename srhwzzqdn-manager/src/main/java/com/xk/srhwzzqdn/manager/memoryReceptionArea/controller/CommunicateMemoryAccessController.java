package com.xk.srhwzzqdn.manager.memoryReceptionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.CommunicateMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.CommunicateMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.CommunicateMemory;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/memoryReception/communicateMemoryAccess")
public class CommunicateMemoryAccessController {
    @Autowired
    private CommunicateMemoryAccessService communicateMemoryAccessService;

    /**
     * 条件分页查询交际记忆列表
     * @param current
     * @param limit
     * @param communicateMemoryDto
     * @return
     */
    @RequestMapping("/getCommunicateMemoryByConditionAndPage/{current}/{limit}")
    public Result getCommunicateMemoryByConditionAndPage(@PathVariable("current") Integer current,
                                                          @PathVariable("limit") Integer limit,
                                                          @RequestBody CommunicateMemoryDto communicateMemoryDto){
        PageInfo<CommunicateMemory> communicateMemoryPageInfo = communicateMemoryAccessService.getCommunicateMemoryByConditionAndPage(current, limit, communicateMemoryDto);
        return Result.build(communicateMemoryPageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存交际记忆
     * @param communicateMemory
     * @return
     */
    @PostMapping("/saveCommunicateMemory")
    public Result saveCommunicateMemory(@RequestBody CommunicateMemory communicateMemory){
        try {
            communicateMemoryAccessService.saveCommunicateMemory(communicateMemory);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存交际记忆失败！");
        }
    }

    /**
     * 根据id删除交际记忆
     * @param id
     * @return
     */
    @DeleteMapping("/deleteCommunicateMemoryById/{id}")
    public Result deleteCommunicateMemoryById(@PathVariable("id") Integer id){
        try {
            communicateMemoryAccessService.deleteCommunicateMemoryById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除交际记忆失败！");
        }
    }

    /**
     * 根据ids批量删除交际记忆
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllCommunicateMemoryByIds")
    public Result deleteAllCommunicateMemoryByIds(@RequestBody List<Integer> ids){
        try {
            communicateMemoryAccessService.deleteAllCommunicateMemoryByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除交际记忆失败！");
        }
    }
}
