package com.xk.srhwzzqdn.manager.memoryReceptionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.CommunicateMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.CommunicateMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.CommunicateMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.CommunicateMemory;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.GenerateNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CommunicateMemoryAccessServiceImpl implements CommunicateMemoryAccessService {
    @Autowired
    private CommunicateMemoryAccessMapper communicateMemoryAccessMapper;

    /**
     * 条件分页查询交际记忆列表
     * @param current
     * @param limit
     * @param communicateMemoryDto
     * @return
     */
    @Override
    public PageInfo<CommunicateMemory> getCommunicateMemoryByConditionAndPage(Integer current, Integer limit, CommunicateMemoryDto communicateMemoryDto) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        communicateMemoryDto.setMemoryOwner(AuthContextUtil.get().getId());

        //条件查询交际记忆列表
        List<CommunicateMemory> communicateMemoryList = communicateMemoryAccessMapper.getCommunicateMemoryByCondition(communicateMemoryDto);

        //创建分页对象
        PageInfo<CommunicateMemory> communicateMemoryPageInfo = new PageInfo<>(communicateMemoryList);

        return communicateMemoryPageInfo;
    }

    /**
     * 保存交际记忆
     * @param communicateMemory
     */
    @Override
    public void saveCommunicateMemory(CommunicateMemory communicateMemory) {
        //计算交际时长(小时)
        communicateMemory.setCommunicateDuration(new BigDecimal((communicateMemory.getEndTime().getTime() - communicateMemory.getBeginTime().getTime())/(1000.0*60*60)));

        if (communicateMemory.getId() == null){//新增
            communicateMemory.setMemoryNo(GenerateNoUtil.generateNo("COM"));
            communicateMemory.setMemoryOwner(AuthContextUtil.get().getId());

            //新增交际记忆
            communicateMemoryAccessMapper.addCommunicateMemory(communicateMemory);
        }else {//修改
            communicateMemory.setUpdateBy(AuthContextUtil.get().getUserName());

            //修改交际记忆
            communicateMemoryAccessMapper.updateCommunicateMemory(communicateMemory);
        }
    }

    /**
     * 根据id删除交际记忆
     * @param id
     */
    @Override
    public void deleteCommunicateMemoryById(Integer id) {
        communicateMemoryAccessMapper.deleteCommunicateMemoryById(id);
    }

    /**
     * 根据ids批量删除交际记忆
     * @param ids
     */
    @Override
    public void deleteAllCommunicateMemoryByIds(List<Integer> ids) {
        communicateMemoryAccessMapper.deleteAllCommunicateMemoryByIds(ids);
    }
}
