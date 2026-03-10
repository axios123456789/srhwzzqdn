package com.xk.srhwzzqdn.manager.memoryReceptionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.FunMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.FunMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.FunMemory;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/memoryReception/funMemoryAccess")
public class FunMemoryAccessController {
    @Autowired
    private FunMemoryAccessService funMemoryAccessService;

    /**
     * 条件分页查询娱乐记忆列表
     * @param current
     * @param limit
     * @param funMemoryDto
     * @return
     */
    @RequestMapping("/getFunMemoryByConditionAndPage/{current}/{limit}")
    public Result getFunMemoryByConditionAndPage(@PathVariable("current") Integer current,
                                                  @PathVariable("limit") Integer limit,
                                                  @RequestBody FunMemoryDto funMemoryDto){
        PageInfo<FunMemory> funMemoryPageInfo = funMemoryAccessService.getFunMemoryByConditionAndPage(current, limit, funMemoryDto);
        return Result.build(funMemoryPageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存娱乐记忆
     * @param funMemory
     * @return
     */
    @PostMapping("/saveFunMemory")
    public Result saveFunMemory(@RequestBody FunMemory funMemory){
        try {
            funMemoryAccessService.saveFunMemory(funMemory);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存娱乐记忆失败！");
        }
    }

    /**
     * 根据id删除娱乐记忆
     * @param id
     * @return
     */
    @DeleteMapping("/deleteFunMemoryById/{id}")
    public Result deleteFunMemoryById(@PathVariable("id") Integer id){
        try {
            funMemoryAccessService.deleteFunMemoryById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除娱乐记忆失败！");
        }
    }

    /**
     * 根据ids批量删除娱乐记忆
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllFunMemoryByIds")
    public Result deleteAllFunMemoryByIds(@RequestBody List<Integer> ids){
        try {
            funMemoryAccessService.deleteAllFunMemoryByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除娱乐记忆失败！");
        }
    }
}
