package com.xk.srhwzzqdn.manager.trialExecutionArea.mapper;

import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TradeRecordDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TradeRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TradeRecordMapper {
    //条件查询交易记录列表
    List<TradeRecord> getTradeRecordByCondition(TradeRecordDto dto);

    //添加交易记录
    void addTradeRecord(TradeRecord tradeRecord);

    //修改交易记录
    void updateTradeRecord(TradeRecord tradeRecord);

    //根据id删除交易记录
    @Delete("delete from t_trial_trade_record where id = #{param1}")
    void deleteTradeRecordById(Long id);

    //根据ids批量删除交易记录
    void deleteAllTradeRecordByIds(List<Long> ids);

    //根据id查询交易记录
    @Select("select * from t_trial_trade_record where id = #{param1}")
    TradeRecord getTradeRecordById(Long id);

    //根据复盘日期汇总当天交易数据
    Map<String, Object> statTradeByReviewDate(@Param("reviewDate") String reviewDate, @Param("owner") String owner);
}