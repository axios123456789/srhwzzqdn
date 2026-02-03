package com.xk.srhwzzqdn.manager.memoryReceptionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.WorkMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.WorkMemory;

import java.util.List;

public interface WorkMemoryAccessService {
    //条件分页查询工作记忆列表
    PageInfo<WorkMemory> getWorkMemoryByConditionAndPage(Integer current, Integer limit, WorkMemoryDto workMemoryDto);

    //保存工作记忆
    void saveWorkMemory(WorkMemory workMemory);

    //根据id删除工作记忆
    void deleteWorkMemoryById(String id);

    //根据ids批量删除工作记忆
    void deleteAllWorkMemoryByIds(List<String> ids);
}
