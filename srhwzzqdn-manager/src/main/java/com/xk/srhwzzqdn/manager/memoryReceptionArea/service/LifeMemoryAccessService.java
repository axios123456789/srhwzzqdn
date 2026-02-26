package com.xk.srhwzzqdn.manager.memoryReceptionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LifeMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LifeMemory;

import java.util.List;

public interface LifeMemoryAccessService {
    //条件分页查询生活记忆列表
    PageInfo<LifeMemory> getLifeMemoryByConditionAndPage(Integer current, Integer limit, LifeMemoryDto lifeMemoryDto);

    //保存生活记忆
    void saveLifeMemory(LifeMemory lifeMemory);

    //根据id删除生活记忆
    void deleteLifeMemoryById(Integer id);

    //根据ids批量删除生活记忆
    void deleteAllLifeMemoryByIds(List<Integer> ids);
}
