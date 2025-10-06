package com.xk.srhwzzqdn.manager.memoryReceptionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.RowMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemory;

import java.util.List;

public interface RowMemoryAccessService {
    //条件分页查询原始记忆列表
    PageInfo<RowMemory> getRowMemoryByConditionAndPage(Integer current, Integer limit, RowMemoryDto rowMemoryDto);

    //保存原始记忆
    void saveRowMemory(RowMemory rowMemory);

    //根据id删除原始记忆
    void deleteRowMemoryById(String id);

    //根据ids批量删除原始记忆
    void deleteAllRowMemoryByIds(List<String> ids);
}
