package com.xk.srhwzzqdn.manager.memoryReceptionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.WorkMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.WorkMemory;

public interface WorkMemoryAccessService {
    //条件分页查询工作记忆列表
    PageInfo<WorkMemory> getWorkMemoryByConditionAndPage(Integer current, Integer limit, WorkMemoryDto workMemoryDto);
}
