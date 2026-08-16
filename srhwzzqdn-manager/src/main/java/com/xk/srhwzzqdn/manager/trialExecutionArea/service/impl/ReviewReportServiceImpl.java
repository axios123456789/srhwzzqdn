package com.xk.srhwzzqdn.manager.trialExecutionArea.service.impl;

import com.xk.srhwzzqdn.manager.trialExecutionArea.mapper.ReviewReportMapper;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.ReviewReportService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.ReviewReportDto;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.ReviewReportVo;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class ReviewReportServiceImpl implements ReviewReportService {
    @Autowired
    private ReviewReportMapper reviewReportMapper;

    /**
     * 获取复盘分析报表（融合每日复盘+交易记录两表数据）
     */
    @Override
    public ReviewReportVo getReviewReport(ReviewReportDto dto) {
        dto.setOwner(AuthContextUtil.get().getId());
        ReviewReportVo vo = new ReviewReportVo();

        // ==================== A. 每日复盘维度 ====================
        //个人绩效KPI
        Map<String, Object> kpiRaw = reviewReportMapper.statReviewKpi(dto);
        ReviewReportVo.ReviewKpi reviewKpi = new ReviewReportVo.ReviewKpi();
        reviewKpi.setTotalReviewDays(getInt(kpiRaw, "totalReviewDays"));
        reviewKpi.setProfitDays(getInt(kpiRaw, "profitDays"));
        reviewKpi.setLossDays(getInt(kpiRaw, "lossDays"));
        reviewKpi.setAvgDailyProfitPct(getBigDecimal(kpiRaw, "avgDailyProfitPct"));
        int winDays = getInt(kpiRaw, "winDays");
        int tradeDays = getInt(kpiRaw, "tradeDays");
        if (tradeDays > 0) {
            reviewKpi.setTotalWinRate(new BigDecimal(winDays).divide(new BigDecimal(tradeDays), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
        } else {
            reviewKpi.setTotalWinRate(BigDecimal.ZERO);
        }
        vo.setReviewKpi(reviewKpi);

        //当日盈亏趋势（同时用于计算最高连续盈利天数）
        List<Map<String, Object>> dailyProfitTrends = reviewReportMapper.statDailyProfitTrends(dto);
        int maxContinuous = 0;
        int currentContinuous = 0;
        for (Map<String, Object> map : dailyProfitTrends) {
            BigDecimal profit = getBigDecimal(map, "profit");
            if (profit.compareTo(BigDecimal.ZERO) > 0) {
                currentContinuous++;
                maxContinuous = Math.max(maxContinuous, currentContinuous);
            } else {
                currentContinuous = 0;
            }
        }
        reviewKpi.setMaxContinuousProfitDays(maxContinuous);
        vo.setDailyProfitTrends(dailyProfitTrends);

        //情绪温度趋势
        vo.setEmotionTrends(reviewReportMapper.statEmotionTrends(dto));
        //市场状态分布
        vo.setMarketStatusStats(reviewReportMapper.statMarketStatus(dto));
        //涨停跌停趋势
        vo.setLimitTrends(reviewReportMapper.statLimitTrends(dto));
        //主线板块频次
        vo.setSectorStats(reviewReportMapper.statSector(dto));
        //适配体系分布
        vo.setAdaptSystemStats(reviewReportMapper.statAdaptSystem(dto));
        //操作自评趋势
        vo.setSelfRatingTrends(reviewReportMapper.statSelfRatingTrends(dto));
        //北向资金趋势
        vo.setNorthTrends(reviewReportMapper.statNorthTrends(dto));

        // ==================== B. 交易记录维度 ====================
        //交易胜率KPI
        Map<String, Object> tradeKpiRaw = reviewReportMapper.statTradeKpi(dto);
        ReviewReportVo.TradeKpi tradeKpi = new ReviewReportVo.TradeKpi();
        int totalTradeCount = getInt(tradeKpiRaw, "totalTradeCount");
        int winCount = getInt(tradeKpiRaw, "winCount");
        tradeKpi.setTotalTradeCount(totalTradeCount);
        tradeKpi.setWinCount(winCount);
        tradeKpi.setLossCount(getInt(tradeKpiRaw, "lossCount"));
        tradeKpi.setAvgProfitPct(getBigDecimal(tradeKpiRaw, "avgProfitPct"));
        tradeKpi.setMaxProfitPct(getBigDecimal(tradeKpiRaw, "maxProfitPct"));
        tradeKpi.setMaxLossPct(getBigDecimal(tradeKpiRaw, "maxLossPct"));
        if (totalTradeCount > 0) {
            tradeKpi.setWinRate(new BigDecimal(winCount).divide(new BigDecimal(totalTradeCount), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
        } else {
            tradeKpi.setWinRate(BigDecimal.ZERO);
        }
        vo.setTradeKpi(tradeKpi);

        //心理状态分布
        vo.setPsychologyStats(reviewReportMapper.statPsychology(dto));
        //心理状态与盈亏关系
        vo.setPsychologyProfits(reviewReportMapper.statPsychologyProfit(dto));
        //交易时段分布
        vo.setTimeSlotStats(reviewReportMapper.statTimeSlot(dto));
        //买卖方向统计
        vo.setDirectionStats(reviewReportMapper.statDirection(dto));
        //执行评分趋势
        vo.setExecuteRatingTrends(reviewReportMapper.statExecuteRatingTrends(dto));
        //是否符合计划统计
        vo.setPlanMatchStats(reviewReportMapper.statPlanMatch(dto));
        //个股交易频次排行
        vo.setStockStats(reviewReportMapper.statStock(dto));

        // ==================== C. 联动分析 ====================
        //每日盈亏vs当日交易笔数
        vo.setDailyProfitVsTradeCounts(reviewReportMapper.statDailyProfitVsTradeCount(dto));
        //情绪温度vs次日交易胜率
        vo.setEmotionVsWinRates(reviewReportMapper.statEmotionVsWinRate(dto));

        return vo;
    }

    /**
     * 从Map中获取int值
     */
    private int getInt(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) return 0;
        return ((Number) map.get(key)).intValue();
    }

    /**
     * 从Map中获取BigDecimal值
     */
    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) return BigDecimal.ZERO;
        return new BigDecimal(map.get(key).toString());
    }
}