package com.xk.srhwzzqdn.manager.memoryReceptionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LifeMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LifeMemory;

public interface LifeMemoryAccessService {
    //条件分页查询生活记忆列表
    PageInfo<LifeMemory> getLifeMemoryByConditionAndPage(Integer current, Integer limit, LifeMemoryDto lifeMemoryDto);
}
