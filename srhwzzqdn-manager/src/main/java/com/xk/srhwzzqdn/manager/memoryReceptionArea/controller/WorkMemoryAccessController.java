package com.xk.srhwzzqdn.manager.memoryReceptionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.WorkMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.WorkMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.WorkMemory;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
