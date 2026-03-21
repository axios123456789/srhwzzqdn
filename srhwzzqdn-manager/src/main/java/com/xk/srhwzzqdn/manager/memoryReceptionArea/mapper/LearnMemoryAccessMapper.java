package com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper;

import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LearnMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LearnMemory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LearnMemoryAccessMapper {
    //条件查询学习记忆列表
    List<LearnMemory> getLearnMemoryByCondition(LearnMemoryDto learnMemoryDto);

    //新增学习记忆
    void addLearnMemory(LearnMemory learnMemory);

    //修改学习记忆
    void updateLearnMemory(LearnMemory learnMemory);

    //根据id删除学习记忆
    @Delete("delete from t_learn_memory where id = #{param1}")
    void deleteLearnMemoryById(Integer id);

    //根据ids批量删除学习记忆
    void deleteAllLearnMemoryByIds(List<Integer> ids);
}
