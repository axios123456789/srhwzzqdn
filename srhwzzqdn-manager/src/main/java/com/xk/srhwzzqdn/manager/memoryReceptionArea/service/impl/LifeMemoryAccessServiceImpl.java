package com.xk.srhwzzqdn.manager.memoryReceptionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.LifeMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.LifeMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LifeMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LifeMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LifeMemoryAccessServiceImpl implements LifeMemoryAccessService {
    @Autowired
    private LifeMemoryAccessMapper lifeMemoryAccessMapper;

    /**
     * 条件分页查询生活记忆列表
     * @param current
     * @param limit
     * @param lifeMemoryDto
     * @return
     */
    @Override
    public PageInfo<LifeMemory> getLifeMemoryByConditionAndPage(Integer current, Integer limit, LifeMemoryDto lifeMemoryDto) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        //条件查询生活记忆列表
        List<LifeMemory> lifeMemoryList = lifeMemoryAccessMapper.getLifeMemoryByCondition(lifeMemoryDto);

        //创建分页对象
        PageInfo<LifeMemory> lifeMemoryPageInfo = new PageInfo<>(lifeMemoryList);

        return lifeMemoryPageInfo;
    }
}
