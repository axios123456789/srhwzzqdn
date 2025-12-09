package com.xk.srhwzzqdn.manager.memoryReceptionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.RowMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.RowMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemory;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemoryConfiguration;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/memoryReception/rowMemoryAccess")
public class RowMemoryAccessController {
    @Autowired
    private RowMemoryAccessService rowMemoryAccessService;

    /**
     * 条件分页查询原始记忆列表
     * @param current
     * @param limit
     * @param rowMemoryDto
     * @return
     */
    @RequestMapping("/getRowMemoryByConditionAndPage/{current}/{limit}")
    public Result getRowMemoryByConditionAndPage(@PathVariable("current") Integer current,
                                                 @PathVariable("limit") Integer limit,
                                                 @RequestBody RowMemoryDto rowMemoryDto){
        PageInfo<RowMemory> pageInfo = rowMemoryAccessService.getRowMemoryByConditionAndPage(current, limit, rowMemoryDto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存原始记忆
     * @param rowMemory
     * @return
     */
    @PostMapping("/saveRowMemory")
    public Result saveRowMemory(@RequestBody RowMemory rowMemory){
        try {
            rowMemoryAccessService.saveRowMemory(rowMemory);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存原始记忆失败！");
        }
    }

    /**
     * 根据id删除原始记忆
     * @param id
     * @return
     */
    @DeleteMapping("/deleteRowMemoryById/{id}")
    public Result deleteRowMemoryById(@PathVariable("id") String id){
        try {
            rowMemoryAccessService.deleteRowMemoryById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除原始记忆失败！");
        }
    }

    /**
     * 根据ids批量删除原始记忆
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllRowMemoryByIds")
    public Result deleteAllRowMemoryByIds(@RequestBody List<String> ids){
        try {
            rowMemoryAccessService.deleteAllRowMemoryByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除原始记忆失败！");
        }
    }

    /**
     * 获取缺失原始记忆的日期列表
     * @return
     */
    @GetMapping("/getLossRowMemoryDate")
    public Result getLossRowMemoryDate(){
        List<String> list = rowMemoryAccessService.getLossRowMemoryDate();
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据时间阶段类型获取原始记忆配置数据
     * @param timePeriodType
     * @return
     */
    @GetMapping("/getMemoryConfigurationByTimeType/{timePeriodType}")
    public Result getMemoryConfigurationByTimeType(@PathVariable("timePeriodType") Integer timePeriodType){
        List<RowMemoryConfiguration> rowMemoryConfigurations = rowMemoryAccessService.getMemoryConfigurationByTimeType(timePeriodType);
        return Result.build(rowMemoryConfigurations, ResultCodeEnum.SUCCESS);
    }
}
