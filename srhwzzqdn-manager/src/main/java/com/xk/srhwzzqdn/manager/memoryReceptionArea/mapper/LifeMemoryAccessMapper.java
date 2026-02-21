package com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper;

import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LifeMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LifeMemory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LifeMemoryAccessMapper {
    //条件查询生活记忆列表
    List<LifeMemory> getLifeMemoryByCondition(LifeMemoryDto lifeMemoryDto);
}
