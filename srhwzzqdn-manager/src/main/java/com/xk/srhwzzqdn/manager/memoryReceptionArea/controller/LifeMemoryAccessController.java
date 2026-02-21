package com.xk.srhwzzqdn.manager.memoryReceptionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.LifeMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LifeMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LifeMemory;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
