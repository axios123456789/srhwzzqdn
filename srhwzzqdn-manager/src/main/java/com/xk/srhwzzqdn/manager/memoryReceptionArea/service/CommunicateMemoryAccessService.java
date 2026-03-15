package com.xk.srhwzzqdn.manager.memoryReceptionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.CommunicateMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.CommunicateMemory;

import java.util.List;

public interface CommunicateMemoryAccessService {
    //条件分页查询交际记忆列表
    PageInfo<CommunicateMemory> getCommunicateMemoryByConditionAndPage(Integer current, Integer limit, CommunicateMemoryDto communicateMemoryDto);

    //保存交际记忆
    void saveCommunicateMemory(CommunicateMemory communicateMemory);

    //根据id删除交际记忆
    void deleteCommunicateMemoryById(Integer id);

    //根据ids批量删除交际记忆
    void deleteAllCommunicateMemoryByIds(List<Integer> ids);
}
