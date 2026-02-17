package com.xk.srhwzzqdn.manager.memoryReceptionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.RowMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.WorkMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.RowMemoryAccessService;
import com.xk.srhwzzqdn.model.dto.memoryReceptionArea.RowMemoryDto;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.AssociativeMemory;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemory;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.RowMemoryConfiguration;
import com.xk.srhwzzqdn.model.entity.memoryReceptionArea.WorkMemory;
import com.xk.srhwzzqdn.util.GenerateNoUtil;
import com.xk.srhwzzqdn.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RowMemoryAccessServiceImpl implements RowMemoryAccessService {
    @Autowired
    private RowMemoryAccessMapper rowMemoryAccessMapper;

    @Autowired
    private WorkMemoryAccessMapper workMemoryAccessMapper;

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

    /**
     * 联想记忆
     * @param associativeMemory
     */
    @Override
    @Transactional
    public void associativeMemory(AssociativeMemory associativeMemory) {
        //1.根据原始记忆类型判断联想什么类型记忆
        if (associativeMemory.getRowMemoryType() == 1){//工作记忆联想
            //数据转换为工作记忆
            WorkMemory workMemory = new WorkMemory();
            workMemory.setId(UUIDUtil.getUUID());
            workMemory.setMemoryNo(GenerateNoUtil.generateNo("WORK"));
            workMemory.setWorkBusinessType(associativeMemory.getWorkBusinessType());
            workMemory.setWorkTechType(associativeMemory.getWorkTechType());
            workMemory.setMemorySource(2);
            workMemory.setBeginTime(associativeMemory.getBegin_time());
            workMemory.setEndTime(associativeMemory.getEnd_time());
            workMemory.setWorkDuration((associativeMemory.getEnd_time().getTime() - associativeMemory.getBegin_time().getTime())/(1000.0*60*60));
            workMemory.setWorkContent(associativeMemory.getRowMemoryContent());
            workMemory.setWorkBusinessNode(associativeMemory.getWorkBusinessNode());
            workMemory.setWorkTechNode(associativeMemory.getWorkTechNode());
            workMemory.setWorkDocument(associativeMemory.getDocument());
            workMemory.setMemoryOwner(associativeMemory.getMemoryOwner());
            workMemory.setWorkAddress(associativeMemory.getMemoryPlace());
            workMemory.setWorkAddressDetail(associativeMemory.getMemoryPlaceDetail());
            workMemory.setMemoryImages(associativeMemory.getMemoryImages());
            workMemory.setRowMemoryNo(associativeMemory.getMemoryNo());

            //生成工作记忆
            workMemoryAccessMapper.addWorkMemory(workMemory);
        } else if (associativeMemory.getRowMemoryType() == 2){//生活记忆联想
            return;
        } else if (associativeMemory.getRowMemoryType() == 3){//娱乐记忆联想
            return;
        } else if (associativeMemory.getRowMemoryType() == 4){//交际记忆联想
            return;
        } else if (associativeMemory.getRowMemoryType() == 5){//学习记忆联想
            return;
        }

        //2.设置对应的联想记忆状态根据记忆类型
        int status = 0;
        switch (associativeMemory.getRowMemoryType()) {
            case 1:
                status = 2;
                break;
            case 2:
                status = 3;
                break;
            case 3:
                status = 4;
                break;
            case 4:
                status = 5;
                break;
            case 5:
                status = 6;
                break;
        }

        //3.原始记忆联想状态改为对应的状态
        rowMemoryAccessMapper.updateRowMemoryStatus(associativeMemory.getRow_id(), status);
    }
}
