package com.xk.srhwzzqdn.manager.memoryReceptionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.WorkMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.WorkMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.WorkMemory;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/memoryReception/workMemoryAccess")
public class WorkMemoryAccessController {
    @Autowired
    private WorkMemoryAccessService workMemoryAccessService;

    /**
     * 条件分页查询工作记忆列表
     * @param current
     * @param limit
     * @param workMemoryDto
     * @return
     */
    @RequestMapping("/getWorkMemoryByConditionAndPage/{current}/{limit}")
    public Result getWorkMemoryByConditionAndPage(@PathVariable("current") Integer current,
                                                  @PathVariable("limit") Integer limit,
                                                  @RequestBody WorkMemoryDto workMemoryDto){
        PageInfo<WorkMemory> pageInfo = workMemoryAccessService.getWorkMemoryByConditionAndPage(current, limit, workMemoryDto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存工作记忆
     * @param workMemory
     * @return
     */
    @PostMapping("/saveWorkMemory")
    public Result saveWorkMemory(@RequestBody WorkMemory workMemory){
        try {
            workMemoryAccessService.saveWorkMemory(workMemory);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存工作记忆失败！");
        }
    }

    /**
     * 根据id删除工作记忆
     * @param id
     * @return
     */
    @DeleteMapping("/deleteWorkMemoryById/{id}")
    public Result deleteWorkMemoryById(@PathVariable("id") String id){
         try {
             workMemoryAccessService.deleteWorkMemoryById(id);
             return Result.build(null, ResultCodeEnum.SUCCESS);
         } catch (Exception e) {
             return Result.build(null, 500, "删除工作记忆失败！");
         }
    }

    /**
     * 根据ids批量删除工作记忆
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllWorkMemoryByIds")
    public Result deleteAllWorkMemoryByIds(@RequestBody List<String> ids){
        try {
            workMemoryAccessService.deleteAllWorkMemoryByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除工作记忆失败！");
        }
    }
}
