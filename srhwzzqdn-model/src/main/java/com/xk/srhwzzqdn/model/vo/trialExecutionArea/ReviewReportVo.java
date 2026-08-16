package com.xk.srhwzzqdn.model.vo.trialExecutionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "复盘分析报表VO")
public class ReviewReportVo {

    // ==================== A. 每日复盘维度 ====================
    @Schema(description = "个人绩效KPI")
    private ReviewKpi reviewKpi;

    @Schema(description = "情绪温度趋势（date, score）")
    private List<Map<String, Object>> emotionTrends;

    @Schema(description = "市场状态分布（code, count）")
    private List<Map<String, Object>> marketStatusStats;

    @Schema(description = "涨停跌停趋势（date, limitUp, limitDown, continuousBoard）")
    private List<Map<String, Object>> limitTrends;

    @Schema(description = "主线板块频次（code, count）")
    private List<Map<String, Object>> sectorStats;

    @Schema(description = "适配体系分布（code, count）")
    private List<Map<String, Object>> adaptSystemStats;

    @Schema(description = "操作自评趋势（date, rating）")
    private List<Map<String, Object>> selfRatingTrends;

    @Schema(description = "当日盈亏趋势（date, profit）")
    private List<Map<String, Object>> dailyProfitTrends;

    @Schema(description = "北向资金趋势（date, amount）")
    private List<Map<String, Object>> northTrends;

    // ==================== B. 交易记录维度 ====================
    @Schema(description = "交易胜率总览KPI")
    private TradeKpi tradeKpi;

    @Schema(description = "心理状态分布（code, count）")
    private List<Map<String, Object>> psychologyStats;

    @Schema(description = "心理状态与盈亏关系（code, avgProfit）")
    private List<Map<String, Object>> psychologyProfits;

    @Schema(description = "交易时段分布（code, count）")
    private List<Map<String, Object>> timeSlotStats;

    @Schema(description = "买卖方向统计（code, count）")
    private List<Map<String, Object>> directionStats;

    @Schema(description = "执行评分趋势（date, rating）")
    private List<Map<String, Object>> executeRatingTrends;

    @Schema(description = "是否符合计划统计（code, count）")
    private List<Map<String, Object>> planMatchStats;

    @Schema(description = "个股交易频次排行（stockName, stockCode, count）")
    private List<Map<String, Object>> stockStats;

    // ==================== C. 联动分析 ====================
    @Schema(description = "每日盈亏vs当日交易笔数（date, profit, tradeCount）")
    private List<Map<String, Object>> dailyProfitVsTradeCounts;

    @Schema(description = "情绪温度vs次日交易胜率（date, emotion, winRate）")
    private List<Map<String, Object>> emotionVsWinRates;

    // ==================== KPI静态类 ====================
    @Data
    @Schema(description = "每日复盘绩效KPI")
    public static class ReviewKpi {
        @Schema(description = "总复盘天数")
        private Integer totalReviewDays;

        @Schema(description = "盈利天数")
        private Integer profitDays;

        @Schema(description = "亏损天数")
        private Integer lossDays;

        @Schema(description = "最高连续盈利天数")
        private Integer maxContinuousProfitDays;

        @Schema(description = "平均每日盈亏（%）")
        private BigDecimal avgDailyProfitPct;

        @Schema(description = "总胜率（%）")
        private BigDecimal totalWinRate;
    }

    @Data
    @Schema(description = "交易记录胜率KPI")
    public static class TradeKpi {
        @Schema(description = "总交易笔数")
        private Integer totalTradeCount;

        @Schema(description = "盈利笔数")
        private Integer winCount;

        @Schema(description = "亏损笔数")
        private Integer lossCount;

        @Schema(description = "胜率（%）")
        private BigDecimal winRate;

        @Schema(description = "平均盈亏（%）")
        private BigDecimal avgProfitPct;

        @Schema(description = "最大单笔盈利（%）")
        private BigDecimal maxProfitPct;

        @Schema(description = "最大单笔亏损（%）")
        private BigDecimal maxLossPct;
    }
}