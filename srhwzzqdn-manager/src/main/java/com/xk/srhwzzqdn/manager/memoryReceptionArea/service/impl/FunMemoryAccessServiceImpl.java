package com.xk.srhwzzqdn.manager.memoryReceptionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.FunMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.FunMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.FunMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.FunMemory;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.GenerateNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FunMemoryAccessServiceImpl implements FunMemoryAccessService {
    @Autowired
    private FunMemoryAccessMapper funMemoryAccessMapper;

    /**
     * 条件分页查询娱乐记忆列表
     * @param current
     * @param limit
     * @param funMemoryDto
     * @return
     */
    @Override
    public PageInfo<FunMemory> getFunMemoryByConditionAndPage(Integer current, Integer limit, FunMemoryDto funMemoryDto) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        funMemoryDto.setMemoryOwner(AuthContextUtil.get().getId());

        //条件查询娱乐记忆列表
        List<FunMemory> funMemoryList = funMemoryAccessMapper.getFunMemoryByCondition(funMemoryDto);

        //创建分页对象
        PageInfo<FunMemory> funMemoryPageInfo = new PageInfo<>(funMemoryList);

        return funMemoryPageInfo;
    }

    /**
     * 保存娱乐记忆
     * @param funMemory
     */
    @Override
    public void saveFunMemory(FunMemory funMemory) {
        //计算时长(小时)
        funMemory.setFunDuration(new BigDecimal((funMemory.getEndTime().getTime() - funMemory.getBeginTime().getTime())/(1000.0*60*60)));

        if (funMemory.getId() == null){//新增
            funMemory.setMemoryNo(GenerateNoUtil.generateNo("FUN"));
            funMemory.setMemoryOwner(AuthContextUtil.get().getId());

            //新增娱乐记忆
            funMemoryAccessMapper.addFunMemory(funMemory);
        }else {//修改
            funMemory.setUpdateBy(AuthContextUtil.get().getUserName());

            //修改娱乐记忆
            funMemoryAccessMapper.updateFunMemory(funMemory);
        }
    }

    /**
     * 根据id删除娱乐记忆
     * @param id
     */
    @Override
    public void deleteFunMemoryById(Integer id) {
        funMemoryAccessMapper.deleteFunMemoryById(id);
    }

    /**
     * 根据ids批量删除娱乐记忆
     * @param ids
     */
    @Override
    public void deleteAllFunMemoryByIds(List<Integer> ids) {
        funMemoryAccessMapper.deleteAllFunMemoryByIds(ids);
    }

    /**
     * 根据id修改娱乐记忆状态
     * @param id
     * @param memoryStatus
     */
    @Override
    public void updateMemoryStatusById(Integer id, Integer memoryStatus) {
        funMemoryAccessMapper.updateMemoryStatusById(id, memoryStatus);
    }
}
