package com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper;

import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.WorkMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.WorkMemory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WorkMemoryAccessMapper {
    //条件查询所有工作记忆列表
    List<WorkMemory> getWorkMemoryByCondition(WorkMemoryDto workMemoryDto);
}
