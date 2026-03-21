package com.xk.srhwzzqdn.manager.memoryReceptionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.LearnMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.LearnMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LearnMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LearnMemory;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.GenerateNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LearnMemoryAccessServiceImpl implements LearnMemoryAccessService {
    @Autowired
    private LearnMemoryAccessMapper learnMemoryAccessMapper;

    /**
     * 条件分页查询学习记忆列表
     * @param current
     * @param limit
     * @param learnMemoryDto
     * @return
     */
    @Override
    public PageInfo<LearnMemory> getLearnMemoryByConditionAndPage(Integer current, Integer limit, LearnMemoryDto learnMemoryDto) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        learnMemoryDto.setMemoryOwner(AuthContextUtil.get().getId());

        //条件查询学习记忆列表
        List<LearnMemory> learnMemoryList = learnMemoryAccessMapper.getLearnMemoryByCondition(learnMemoryDto);

        //创建分页对象
        PageInfo<LearnMemory> learnMemoryPageInfo = new PageInfo<>(learnMemoryList);

        return learnMemoryPageInfo;
    }

    /**
     * 保存学习记忆
     * @param learnMemory
     */
    @Override
    public void saveLearnMemory(LearnMemory learnMemory) {
        //计算时长(小时)
        learnMemory.setLearnDuration(new BigDecimal((learnMemory.getEndTime().getTime() - learnMemory.getBeginTime().getTime())/(1000.0*60*60)));

        if (learnMemory.getId() == null){//新增
            learnMemory.setMemoryNo(GenerateNoUtil.generateNo("LEARN"));
            learnMemory.setMemoryOwner(AuthContextUtil.get().getId());

            //新增学习记忆
            learnMemoryAccessMapper.addLearnMemory(learnMemory);
        }else {//修改
            learnMemory.setUpdateBy(AuthContextUtil.get().getUserName());

            //修改学习记忆
            learnMemoryAccessMapper.updateLearnMemory(learnMemory);
        }
    }

    /**
     * 根据id删除学习记忆
     * @param id
     */
    @Override
    public void deleteLearnMemoryById(Integer id) {
        learnMemoryAccessMapper.deleteLearnMemoryById(id);
    }

    /**
     * 根据ids批量删除学习记忆
     * @param ids
     */
    @Override
    public void deleteAllLearnMemoryByIds(List<Integer> ids) {
        learnMemoryAccessMapper.deleteAllLearnMemoryByIds(ids);
    }
}
