package com.xk.srhwzzqdn.manager.memoryReceptionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.RowMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.RowMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.RowMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemory;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemoryConfiguration;
import com.xk.srhwzzqdn.util.GenerateNoUtil;
import com.xk.srhwzzqdn.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xk.srhwzzqdn.util.AuthContextUtil;

import java.util.List;

@Service
public class RowMemoryAccessServiceImpl implements RowMemoryAccessService {
    @Autowired
    private RowMemoryAccessMapper rowMemoryAccessMapper;

    /**
     * 条件分页查询原始记忆列表
     * @param current
     * @param limit
     * @param rowMemoryDto
     * @return
     */
    @Override
    public PageInfo<RowMemory> getRowMemoryByConditionAndPage(Integer current, Integer limit, RowMemoryDto rowMemoryDto) {
        //开启分页，下一个数据库查询结果将使用分页插件查询
        PageHelper.startPage(current, limit);

        //设置查询条件（记忆所属人）
        rowMemoryDto.setMemoryOwner(AuthContextUtil.get().getId());

        //条件查询所有原始记忆列表
        List<RowMemory> rowMemoryList = rowMemoryAccessMapper.getRowMemoryByCondition(rowMemoryDto);

        //设置分页
        PageInfo<RowMemory> pageInfo = new PageInfo<>(rowMemoryList);

        return pageInfo;
    }

    /**
     * 保存原始记忆
     * @param rowMemory
     */
    @Override
    public void saveRowMemory(RowMemory rowMemory) {
        if (rowMemory.getId() == null || "".equals(rowMemory.getId())){//添加
            rowMemory.setId(UUIDUtil.getUUID());
            rowMemory.setMemoryOwner(AuthContextUtil.get().getId());
            rowMemory.setRecordBy(AuthContextUtil.get().getUserName());
            rowMemory.setMemoryNo(GenerateNoUtil.generateNo("ROW"));
            //添加
            rowMemoryAccessMapper.addRowMemory(rowMemory);
        }else {//修改
            rowMemory.setUpdateBy(AuthContextUtil.get().getUserName());

            //修改
            rowMemoryAccessMapper.updateRowMemory(rowMemory);
        }
    }

    /**
     * 根据id删除原始记忆
     * @param id
     */
    @Override
    public void deleteRowMemoryById(String id) {
        rowMemoryAccessMapper.deleteRowMemoryById(id);
    }

    /**
     * 根据ids批量删除原始记忆
     * @param ids
     */
    @Override
    public void deleteAllRowMemoryByIds(List<String> ids) {
        rowMemoryAccessMapper.deleteAllRowMemoryByIds(ids);
    }

    /**
     * 获取缺失原始记忆的日期列表
     * @return
     */
    @Override
    public List<String> getLossRowMemoryDate() {
        List<String> list = rowMemoryAccessMapper.getLossRowMemoryDate();
        return list;
    }

    /**
     * 根据时间阶段类型获取原始记忆配置数据
     * @param timePeriodType
     * @return
     */
    @Override
    public List<RowMemoryConfiguration> getMemoryConfigurationByTimeType(Integer timePeriodType) {
        List<RowMemoryConfiguration> rowMemoryConfigurations = rowMemoryAccessMapper.getMemoryConfigurationByTimeType(timePeriodType);
        return rowMemoryConfigurations;
    }
}
