package com.xk.srhwzzqdn.manager.memoryReceptionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.WorkMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.WorkMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.WorkMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.WorkMemory;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkMemoryAccessServiceImpl implements WorkMemoryAccessService {
    @Autowired
    private WorkMemoryAccessMapper workMemoryAccessMapper;

    /**
     * 条件分页查询工作记忆列表
     * @param current
     * @param limit
     * @param workMemoryDto
     * @return
     */
    @Override
    public PageInfo<WorkMemory> getWorkMemoryByConditionAndPage(Integer current, Integer limit, WorkMemoryDto workMemoryDto) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        //2.//设置查询条件（记忆所属人）
        workMemoryDto.setMemoryOwner(AuthContextUtil.get().getId());

        //3.条件查询所有工作记忆列表
        List<WorkMemory> workMemoryList = workMemoryAccessMapper.getWorkMemoryByCondition(workMemoryDto);

        //4.设置分页
        PageInfo<WorkMemory> workMemoryPageInfo = new PageInfo<>(workMemoryList);

        return workMemoryPageInfo;
    }
}
