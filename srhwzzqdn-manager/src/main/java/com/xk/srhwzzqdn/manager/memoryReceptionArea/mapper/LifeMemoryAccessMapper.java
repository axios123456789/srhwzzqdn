package com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper;

import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LifeMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LifeMemory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LifeMemoryAccessMapper {
    //条件查询生活记忆列表
    List<LifeMemory> getLifeMemoryByCondition(LifeMemoryDto lifeMemoryDto);

    //新增生活记忆
    void addLifeMemory(LifeMemory lifeMemory);

    //修改生活记忆
    void updateLifeMemory(LifeMemory lifeMemory);

    //根据id删除生活记忆
    @Delete("delete from t_life_memory where id = #{param1}")
    void deleteLifeMemoryById(Integer id);

    //根据ids批量删除生活记忆
    void deleteAllLifeMemoryByIds(List<Integer> ids);
}
