package com.xk.srhwzzqdn.manager.trialExecutionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.mapper.TradeRecordMapper;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.TradeRecordService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TradeRecordDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TradeRecord;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TradeRecordServiceImpl implements TradeRecordService {
    @Autowired
    private TradeRecordMapper tradeRecordMapper;

    /**
     * 条件分页查询交易记录列表
     */
    @Override
    public PageInfo<TradeRecord> getTradeRecordByConditionAndPage(Integer current, Integer limit, TradeRecordDto dto) {
        PageHelper.startPage(current, limit);
        dto.setOwner(AuthContextUtil.get().getId());
        List<TradeRecord> list = tradeRecordMapper.getTradeRecordByCondition(dto);
        return new PageInfo<>(list);
    }

    /**
     * 保存交易记录
     */
    @Override
    public void saveTradeRecord(TradeRecord tradeRecord) {
        if (tradeRecord.getId() == null) {
            tradeRecord.setOwner(AuthContextUtil.get().getId());
            tradeRecordMapper.addTradeRecord(tradeRecord);
        } else {
            tradeRecordMapper.updateTradeRecord(tradeRecord);
        }
    }

    /**
     * 根据id删除交易记录
     */
    @Override
    public void deleteTradeRecordById(Long id) {
        tradeRecordMapper.deleteTradeRecordById(id);
    }

    /**
     * 根据ids批量删除交易记录
     */
    @Override
    public void deleteAllTradeRecordByIds(List<Long> ids) {
        tradeRecordMapper.deleteAllTradeRecordByIds(ids);
    }

    /**
     * 根据复盘日期汇总当天交易数据
     */
    @Override
    public Map<String, Object> statTradeByReviewDate(String reviewDate) {
        String owner = AuthContextUtil.get().getId();
        return tradeRecordMapper.statTradeByReviewDate(reviewDate, owner);
    }
}