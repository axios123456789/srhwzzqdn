package com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper;

import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.WorkMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.WorkMemory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WorkMemoryAccessMapper {
    //条件查询所有工作记忆列表
    List<WorkMemory> getWorkMemoryByCondition(WorkMemoryDto workMemoryDto);

    //添加工作记忆
    void addWorkMemory(WorkMemory workMemory);

    //修改工作记忆
    void updateWorkMemory(WorkMemory workMemory);

    //根据id删除工作记忆
    @Delete("delete from t_work_memory where id = #{param1}")
    void deleteWorkMemoryById(String id);

    //根据ids批量删除工作记忆
    void deleteAllWorkMemoryByIds(List<String> ids);
}
