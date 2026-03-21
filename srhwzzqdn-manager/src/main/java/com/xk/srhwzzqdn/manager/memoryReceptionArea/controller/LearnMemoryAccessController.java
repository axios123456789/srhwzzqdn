package com.xk.srhwzzqdn.manager.memoryReceptionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.LearnMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LearnMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LearnMemory;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/memoryReception/learnMemoryAccess")
public class LearnMemoryAccessController {
    @Autowired
    private LearnMemoryAccessService learnMemoryAccessService;

    /**
     * 条件分页查询学习记忆列表
     * @param current
     * @param limit
     * @param learnMemoryDto
     * @return
     */
    @RequestMapping("/getLearnMemoryByConditionAndPage/{current}/{limit}")
    public Result getLearnMemoryByConditionAndPage(@PathVariable("current") Integer current,
                                                  @PathVariable("limit") Integer limit,
                                                  @RequestBody LearnMemoryDto learnMemoryDto){
        PageInfo<LearnMemory> learnMemoryPageInfo = learnMemoryAccessService.getLearnMemoryByConditionAndPage(current, limit, learnMemoryDto);
        return Result.build(learnMemoryPageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存学习记忆
     * @param learnMemory
     * @return
     */
    @PostMapping("/saveLearnMemory")
    public Result saveLearnMemory(@RequestBody LearnMemory learnMemory){
        try {
            learnMemoryAccessService.saveLearnMemory(learnMemory);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存学习记忆失败！");
        }
    }

    /**
     * 根据id删除学习记忆
     * @param id
     * @return
     */
    @DeleteMapping("/deleteLearnMemoryById/{id}")
    public Result deleteLearnMemoryById(@PathVariable("id") Integer id){
        try {
            learnMemoryAccessService.deleteLearnMemoryById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除学习记忆失败！");
        }
    }

    /**
     * 根据ids批量删除学习记忆
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllLearnMemoryByIds")
    public Result deleteAllLearnMemoryByIds(@RequestBody List<Integer> ids){
        try {
            learnMemoryAccessService.deleteAllLearnMemoryByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除学习记忆失败！");
        }
    }
}
