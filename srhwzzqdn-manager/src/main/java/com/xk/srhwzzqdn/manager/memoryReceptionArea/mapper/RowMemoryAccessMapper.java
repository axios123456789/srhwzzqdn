package com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper;

import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.RowMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RowMemoryAccessMapper {
    //条件查询所有原始记忆列表
    List<RowMemory> getRowMemoryByCondition(RowMemoryDto rowMemoryDto);

    //添加原始记忆
    void addRowMemory(RowMemory rowMemory);

    //修改原始记忆
    void updateRowMemory(RowMemory rowMemory);

    //根据id删除原始记忆
    @Delete("delete from t_row_memory where id = #{param1}")
    void deleteRowMemoryById(String id);

    //根据ids批量删除原始记忆
    void deleteAllRowMemoryByIds(List<String> ids);
}
