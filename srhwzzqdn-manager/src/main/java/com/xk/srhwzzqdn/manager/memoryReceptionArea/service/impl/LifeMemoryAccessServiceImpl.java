package com.xk.srhwzzqdn.manager.memoryReceptionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.LifeMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.LifeMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.LifeMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.LifeMemory;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.GenerateNoUtil;
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

    /**
     * 保存生活记忆
     * @param lifeMemory
     */
    @Override
    public void saveLifeMemory(LifeMemory lifeMemory) {
        if (lifeMemory.getId() == null){//新增
            lifeMemory.setMemoryNo(GenerateNoUtil.generateNo("LIFE"));
            lifeMemory.setMemoryOwner(AuthContextUtil.get().getId());

            //新增生活记忆
            lifeMemoryAccessMapper.addLifeMemory(lifeMemory);
        }else {//修改
            lifeMemory.setUpdateBy(AuthContextUtil.get().getUserName());

            //修改生活记忆
            lifeMemoryAccessMapper.updateLifeMemory(lifeMemory);
        }
    }

    /**
     * 根据id删除生活记忆
     * @param id
     */
    @Override
    public void deleteLifeMemoryById(Integer id) {
        lifeMemoryAccessMapper.deleteLifeMemoryById(id);
    }

    /**
     * 根据ids批量删除生活记忆
     * @param ids
     */
    @Override
    public void deleteAllLifeMemoryByIds(List<Integer> ids) {
        lifeMemoryAccessMapper.deleteAllLifeMemoryByIds(ids);
    }
}
