package com.xk.srhwzzqdn.manager.trialExecutionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TradeRecordDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TradeRecord;

import java.util.List;
import java.util.Map;

public interface TradeRecordService {
    //条件分页查询交易记录列表
    PageInfo<TradeRecord> getTradeRecordByConditionAndPage(Integer current, Integer limit, TradeRecordDto dto);

    //保存交易记录
    void saveTradeRecord(TradeRecord tradeRecord);

    //根据id删除交易记录
    void deleteTradeRecordById(Long id);

    //根据ids批量删除交易记录
    void deleteAllTradeRecordByIds(List<Long> ids);

    //根据复盘日期汇总当天交易数据
    Map<String, Object> statTradeByReviewDate(String reviewDate);
}