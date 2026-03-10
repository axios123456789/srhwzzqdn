package com.xk.srhwzzqdn.manager.memoryReceptionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.FunMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.FunMemory;

import java.util.List;

public interface FunMemoryAccessService {
    //条件分页查询娱乐记忆列表
    PageInfo<FunMemory> getFunMemoryByConditionAndPage(Integer current, Integer limit, FunMemoryDto funMemoryDto);

    //保存娱乐记忆
    void saveFunMemory(FunMemory funMemory);

    //根据id删除娱乐记忆
    void deleteFunMemoryById(Integer id);

    //根据ids批量删除娱乐记忆
    void deleteAllFunMemoryByIds(List<Integer> ids);
}
