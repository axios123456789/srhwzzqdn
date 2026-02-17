package com.xk.srhwzzqdn.manager.memoryReceptionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.RowMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.AssociativeMemory;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemory;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemoryConfiguration;

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

    //获取缺失原始记忆的日期列表
    List<String> getLossRowMemoryDate();

    //根据时间阶段类型获取原始记忆配置数据
    List<RowMemoryConfiguration> getMemoryConfigurationByTimeType(Integer timePeriodType);

    //联想记忆
    void associativeMemory(AssociativeMemory associativeMemory);
}
