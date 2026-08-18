package com.xk.srhwzzqdn.manager.trialExecutionArea.controller;

import com.xk.srhwzzqdn.manager.trialExecutionArea.service.ReviewReportService;
import com.xk.srhwzzqdn.manager.util.AiCommonUtil;
import com.xk.srhwzzqdn.manager.util.AiPromptUtil;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.ReviewReportDto;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.ReviewReportVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superBrain/trialExecution/reviewAnalysis")
public class ReviewReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReviewReportController.class);

    @Autowired
    private ReviewReportService reviewReportService;

    @Autowired
    private AiCommonUtil aiCommonUtil;

    /**
     * 获取复盘分析报表（融合每日复盘+交易记录两表数据）
     */
    @PostMapping("/getReviewReport")
    public Result getReviewReport(@RequestBody ReviewReportDto dto) {
        ReviewReportVo vo = reviewReportService.getReviewReport(dto);
        return Result.build(vo, ResultCodeEnum.SUCCESS);
    }

    /**
     * AI生成复盘分析报告（Markdown格式）
     */
    @PostMapping("/aiGenerate")
    public Result aiGenerate(@RequestBody ReviewReportDto dto) {
        try {
            // 先查询统计数据
            ReviewReportVo vo = reviewReportService.getReviewReport(dto);

            // 计算时间范围天数
            int rangeDays = 30;
            if (dto.getStartTime() != null && dto.getEndTime() != null) {
                long diff = (java.sql.Date.valueOf(dto.getEndTime()).getTime() - java.sql.Date.valueOf(dto.getStartTime()).getTime()) / (24 * 3600 * 1000);
                rangeDays = (int) diff;
            }

            // 构建KPI摘要
            ReviewReportVo.ReviewKpi rk = vo.getReviewKpi();
            ReviewReportVo.TradeKpi tk = vo.getTradeKpi();

            String prompt = AiPromptUtil.buildReviewReportPrompt(rangeDays, new Object[]{
                    rk != null ? AiPromptUtil.safeNum(rk.getTotalReviewDays()) : "0",
                    rk != null ? AiPromptUtil.safeNum(rk.getProfitDays()) : "0",
                    rk != null ? AiPromptUtil.safeNum(rk.getLossDays()) : "0",
                    rk != null ? AiPromptUtil.safeNum(rk.getMaxContinuousProfitDays()) : "0",
                    rk != null ? AiPromptUtil.safeNum(rk.getAvgDailyProfitPct()) : "0",
                    tk != null ? AiPromptUtil.safeNum(tk.getTotalTradeCount()) : "0",
                    tk != null ? AiPromptUtil.safeNum(tk.getWinCount()) : "0",
                    tk != null ? AiPromptUtil.safeNum(tk.getLossCount()) : "0",
                    tk != null ? AiPromptUtil.safeNum(tk.getWinRate()) : "0",
                    tk != null ? AiPromptUtil.safeNum(tk.getAvgProfitPct()) : "0",
                    tk != null ? AiPromptUtil.safeNum(tk.getMaxProfitPct()) : "0",
                    tk != null ? AiPromptUtil.safeNum(tk.getMaxLossPct()) : "0",
                    summarizeTrends(vo.getEmotionTrends(), "score"),
                    summarizeStats(vo.getMarketStatusStats()),
                    summarizeStats(vo.getPsychologyStats()),
                    summarizeTrends(vo.getSelfRatingTrends(), "rating"),
                    summarizeTrends(vo.getExecuteRatingTrends(), "rating"),
                    summarizeStats(vo.getAdaptSystemStats())
            });

            String report = aiCommonUtil.callWithSystem(AiPromptUtil.REVIEW_REPORT_SYSTEM, prompt);
            if (report == null || report.trim().isEmpty()) {
                return Result.build(null, 500, "AI生成失败，请检查AI配置");
            }

            return Result.build(Map.of("report", report), ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("AI生成复盘分析报告失败", e);
            return Result.build(null, 500, "AI生成失败：" + e.getMessage());
        }
    }

    /**
     * 汇总趋势数据为摘要字符串
     */
    private String summarizeTrends(List<Map<String, Object>> trends, String valueKey) {
        if (trends == null || trends.isEmpty()) return "无数据";
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(trends.size(), 10);
        for (int i = 0; i < limit; i++) {
            Map<String, Object> item = trends.get(i);
            if (i > 0) sb.append(", ");
            sb.append(item.get("date")).append(":").append(item.get(valueKey));
        }
        if (trends.size() > limit) sb.append("...共").append(trends.size()).append("条");
        return sb.toString();
    }

    /**
     * 汇总分布统计数据为摘要字符串
     */
    private String summarizeStats(List<Map<String, Object>> stats) {
        if (stats == null || stats.isEmpty()) return "无数据";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stats.size(); i++) {
            Map<String, Object> item = stats.get(i);
            if (i > 0) sb.append(", ");
            sb.append("code=").append(item.get("code")).append("→").append(item.get("count")).append("次");
        }
        return sb.toString();
    }
}