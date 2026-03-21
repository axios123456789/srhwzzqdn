package com.xk.srhwzzqdn.manager.memoryReceptionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LearnMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LearnMemory;

import java.util.List;

public interface LearnMemoryAccessService {
    //条件分页查询学习记忆列表
    PageInfo<LearnMemory> getLearnMemoryByConditionAndPage(Integer current, Integer limit, LearnMemoryDto learnMemoryDto);

    //保存学习记忆
    void saveLearnMemory(LearnMemory learnMemory);

    //根据id删除学习记忆
    void deleteLearnMemoryById(Integer id);

    //根据ids批量删除学习记忆
    void deleteAllLearnMemoryByIds(List<Integer> ids);
}
