package com.xk.srhwzzqdn.manager.trialExecutionArea.mapper;

import com.xk.srhwzzqdn.model.dto.trialExecutionArea.ReviewReportDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewReportMapper {
    // ==================== A. 每日复盘维度 ====================
    //个人绩效KPI
    Map<String, Object> statReviewKpi(ReviewReportDto dto);

    //情绪温度趋势
    List<Map<String, Object>> statEmotionTrends(ReviewReportDto dto);

    //市场状态分布
    List<Map<String, Object>> statMarketStatus(ReviewReportDto dto);

    //涨停跌停趋势
    List<Map<String, Object>> statLimitTrends(ReviewReportDto dto);

    //主线板块频次
    List<Map<String, Object>> statSector(ReviewReportDto dto);

    //适配体系分布
    List<Map<String, Object>> statAdaptSystem(ReviewReportDto dto);

    //操作自评趋势
    List<Map<String, Object>> statSelfRatingTrends(ReviewReportDto dto);

    //当日盈亏趋势
    List<Map<String, Object>> statDailyProfitTrends(ReviewReportDto dto);

    //北向资金趋势
    List<Map<String, Object>> statNorthTrends(ReviewReportDto dto);

    // ==================== B. 交易记录维度 ====================
    //交易胜率KPI
    Map<String, Object> statTradeKpi(ReviewReportDto dto);

    //心理状态分布
    List<Map<String, Object>> statPsychology(ReviewReportDto dto);

    //心理状态与盈亏关系
    List<Map<String, Object>> statPsychologyProfit(ReviewReportDto dto);

    //交易时段分布
    List<Map<String, Object>> statTimeSlot(ReviewReportDto dto);

    //买卖方向统计
    List<Map<String, Object>> statDirection(ReviewReportDto dto);

    //执行评分趋势
    List<Map<String, Object>> statExecuteRatingTrends(ReviewReportDto dto);

    //是否符合计划统计
    List<Map<String, Object>> statPlanMatch(ReviewReportDto dto);

    //个股交易频次排行
    List<Map<String, Object>> statStock(ReviewReportDto dto);

    // ==================== C. 联动分析 ====================
    //每日盈亏vs当日交易笔数
    List<Map<String, Object>> statDailyProfitVsTradeCount(ReviewReportDto dto);

    //情绪温度vs次日交易胜率
    List<Map<String, Object>> statEmotionVsWinRate(ReviewReportDto dto);
}