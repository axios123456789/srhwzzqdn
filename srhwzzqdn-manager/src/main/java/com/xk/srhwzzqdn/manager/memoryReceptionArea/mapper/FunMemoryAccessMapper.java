package com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper;

import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.FunMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.FunMemory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FunMemoryAccessMapper {
    //条件查询娱乐记忆列表
    List<FunMemory> getFunMemoryByCondition(FunMemoryDto funMemoryDto);

    //新增娱乐记忆
    void addFunMemory(FunMemory funMemory);

    //修改娱乐记忆
    void updateFunMemory(FunMemory funMemory);

    //根据id删除娱乐记忆
    @Delete("delete from t_fun_memory where id = #{param1}")
    void deleteFunMemoryById(Integer id);

    //根据ids批量删除娱乐记忆
    void deleteAllFunMemoryByIds(List<Integer> ids);
}
