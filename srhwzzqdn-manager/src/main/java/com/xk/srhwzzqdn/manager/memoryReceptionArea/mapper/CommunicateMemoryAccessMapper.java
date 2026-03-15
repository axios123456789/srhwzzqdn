package com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper;

import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.CommunicateMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.CommunicateMemory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommunicateMemoryAccessMapper {
    //条件查询交际记忆列表
    List<CommunicateMemory> getCommunicateMemoryByCondition(CommunicateMemoryDto communicateMemoryDto);

    //新增交际记忆
    void addCommunicateMemory(CommunicateMemory communicateMemory);

    //修改交际记忆
    void updateCommunicateMemory(CommunicateMemory communicateMemory);

    //根据id删除交际记忆
    @Delete("delete from t_communicate_memory where id = #{param1}")
    void deleteCommunicateMemoryById(Integer id);

    //根据ids批量删除交际记忆
    void deleteAllCommunicateMemoryByIds(List<Integer> ids);
}
