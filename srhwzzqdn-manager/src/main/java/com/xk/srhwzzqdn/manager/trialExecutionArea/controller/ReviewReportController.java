package com.xk.srhwzzqdn.manager.trialExecutionArea.controller;

import com.xk.srhwzzqdn.manager.trialExecutionArea.service.ReviewReportService;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.TradeRecordService;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.PredictionSimulateService;
import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetTransactionService;
import com.xk.srhwzzqdn.manager.util.AiCommonUtil;
import com.xk.srhwzzqdn.manager.util.AiPromptUtil;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.ReviewReportDto;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TradeRecordDto;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionSimulateDto;
import com.xk.srhwzzqdn.model.dto.assetControl.AssetTransactionDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TradeRecord;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.PredictionSimulate;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetTransactionVo;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.ReviewReportVo;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/superBrain/trialExecution/reviewAnalysis")
public class ReviewReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReviewReportController.class);

    @Autowired
    private ReviewReportService reviewReportService;

    @Autowired
    private TradeRecordService tradeRecordService;

    @Autowired
    private PredictionSimulateService predictionSimulateService;

    @Autowired
    private AssetTransactionService assetTransactionService;

    @Autowired
    private AiCommonUtil aiCommonUtil;

    // 交易心理状态字典（t_trial_trade_psychology）
    private static final Map<Integer, String> PSYCHOLOGY_MAP = new HashMap<>();
    private static final Map<Integer, String> DIRECTION_MAP = new HashMap<>();
    private static final Map<Integer, String> FOLLOW_PLAN_MAP = new HashMap<>();
    private static final Map<Integer, String> RATING_MAP = new HashMap<>();
    static {
        PSYCHOLOGY_MAP.put(1, "贪婪"); PSYCHOLOGY_MAP.put(2, "恐惧"); PSYCHOLOGY_MAP.put(3, "犹豫");
        PSYCHOLOGY_MAP.put(4, "果断"); PSYCHOLOGY_MAP.put(5, "冲动"); PSYCHOLOGY_MAP.put(6, "理性");
        PSYCHOLOGY_MAP.put(7, "从众"); PSYCHOLOGY_MAP.put(8, "独立"); PSYCHOLOGY_MAP.put(9, "报复");
        PSYCHOLOGY_MAP.put(10, "侥幸"); PSYCHOLOGY_MAP.put(11, "FOMO");
        DIRECTION_MAP.put(1, "买入"); DIRECTION_MAP.put(2, "卖出"); DIRECTION_MAP.put(3, "加仓");
        DIRECTION_MAP.put(4, "减仓"); DIRECTION_MAP.put(5, "清仓"); DIRECTION_MAP.put(6, "T+0");
        FOLLOW_PLAN_MAP.put(1, "符合"); FOLLOW_PLAN_MAP.put(2, "不符合");
        FOLLOW_PLAN_MAP.put(3, "部分符合"); FOLLOW_PLAN_MAP.put(4, "无计划随意");
        RATING_MAP.put(1, "1星-很差"); RATING_MAP.put(2, "2星-较差"); RATING_MAP.put(3, "3星-一般");
        RATING_MAP.put(4, "4星-良好"); RATING_MAP.put(5, "5星-优秀");
    }

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
     * 聚合：复盘KPI+交易记录明细+预测准确率+资产收支+心理分布，传给AI生成诊断报告
     */
    @PostMapping("/aiGenerate")
    public Result aiGenerate(@RequestBody ReviewReportDto dto) {
        try {
            ReviewReportVo vo = reviewReportService.getReviewReport(dto);

            int rangeDays = 30;
            if (dto.getStartTime() != null && dto.getEndTime() != null) {
                long diff = (java.sql.Date.valueOf(dto.getEndTime()).getTime() - java.sql.Date.valueOf(dto.getStartTime()).getTime()) / (24 * 3600 * 1000);
                rangeDays = (int) diff;
            }

            StringBuilder summary = new StringBuilder();
            summary.append(buildKpiSummary(vo));
            summary.append(buildTradeDetailSummary());
            summary.append(buildPredictionSummary());
            summary.append(buildAssetSummary());
            summary.append(buildDistSummary(vo));

            String prompt = AiPromptUtil.buildReviewReportPrompt(rangeDays, summary.toString());
            String report = aiCommonUtil.callWithSystem(AiPromptUtil.REVIEW_REPORT_SYSTEM, prompt);
            if (report == null || report.trim().isEmpty()) {
                return Result.build(null, 500, "AI生成失败，请检查AI配置");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("report", report);
            data.put("dataSummary", summary.toString());
            return Result.build(data, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("AI生成复盘分析报告失败", e);
            return Result.build(null, 500, "AI生成失败：" + e.getMessage());
        }
    }

    /**
     * 复盘KPI+交易KPI摘要
     */
    private String buildKpiSummary(ReviewReportVo vo) {
        StringBuilder sb = new StringBuilder();
        sb.append("【复盘KPI】\n");
        ReviewReportVo.ReviewKpi rk = vo.getReviewKpi();
        ReviewReportVo.TradeKpi tk = vo.getTradeKpi();
        if (rk != null) {
            sb.append("总复盘天数:").append(rk.getTotalReviewDays())
                    .append(" 盈利天数:").append(rk.getProfitDays())
                    .append(" 亏损天数:").append(rk.getLossDays())
                    .append(" 最高连续盈利:").append(rk.getMaxContinuousProfitDays()).append("天")
                    .append(" 平均每日盈亏:").append(rk.getAvgDailyProfitPct()).append("%\n");
        }
        if (tk != null) {
            sb.append("【交易KPI】\n总交易笔数:").append(tk.getTotalTradeCount())
                    .append(" 盈利:").append(tk.getWinCount())
                    .append(" 亏损:").append(tk.getLossCount())
                    .append(" 胜率:").append(tk.getWinRate()).append("%")
                    .append(" 平均盈亏:").append(tk.getAvgProfitPct()).append("%")
                    .append(" 最大盈利:").append(tk.getMaxProfitPct()).append("%")
                    .append(" 最大亏损:").append(tk.getMaxLossPct()).append("%\n");
        }
        return sb.toString();
    }

    /**
     * 交易记录明细（含反思）+ 心理状态与盈亏关系聚合 —— 找坏习惯的核心数据
     */
    private String buildTradeDetailSummary() {
        StringBuilder sb = new StringBuilder();
        try {
            List<TradeRecord> records = tradeRecordService
                    .getTradeRecordByConditionAndPage(1, 50, new TradeRecordDto()).getList();
            if (records == null || records.isEmpty()) {
                sb.append("【交易记录明细】无数据\n");
                return sb.toString();
            }
            sb.append("【交易记录明细（最近").append(records.size()).append("笔，含反思）】\n");
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
            // 按心理状态聚合盈亏
            Map<Integer, List<Double>> psychoProfit = new LinkedHashMap<>();
            int noPlanCount = 0, violatePlanCount = 0;
            for (TradeRecord r : records) {
                sb.append(sdf.format(r.getTradeDatetime())).append(" ")
                        .append(r.getStockName()).append(" ")
                        .append(DIRECTION_MAP.getOrDefault(r.getTradeDirection(), "?"));
                if (r.getProfitPct() != null) sb.append(" 盈亏").append(r.getProfitPct()).append("%");
                if (r.getPsychology() != null) sb.append(" 心理:").append(PSYCHOLOGY_MAP.getOrDefault(r.getPsychology(), "?"));
                if (r.getFollowPlan() != null) {
                    sb.append(" 计划:").append(FOLLOW_PLAN_MAP.getOrDefault(r.getFollowPlan(), "?"));
                    if (r.getFollowPlan() == 2) violatePlanCount++;
                    if (r.getFollowPlan() == 4) noPlanCount++;
                }
                if (r.getExecuteRating() != null) sb.append(" 执行:").append(RATING_MAP.getOrDefault(r.getExecuteRating(), "?"));
                if (r.getHoldingDuration() != null) sb.append(" 持仓").append(r.getHoldingDuration()).append("天");
                if (r.getReflection() != null && !r.getReflection().trim().isEmpty()) {
                    sb.append(" 反思:").append(r.getReflection().trim());
                }
                sb.append("\n");
                if (r.getPsychology() != null && r.getProfitPct() != null) {
                    psychoProfit.computeIfAbsent(r.getPsychology(), k -> new ArrayList<>())
                            .add(r.getProfitPct().doubleValue());
                }
            }
            // 心理-盈亏关系
            sb.append("【心理状态与盈亏关系】\n");
            for (Map.Entry<Integer, List<Double>> e : psychoProfit.entrySet()) {
                List<Double> profits = e.getValue();
                double avg = profits.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                long win = profits.stream().filter(p -> p > 0).count();
                sb.append(PSYCHOLOGY_MAP.getOrDefault(e.getKey(), "?"))
                        .append(": ").append(profits.size()).append("笔, 平均盈亏")
                        .append(new BigDecimal(avg).setScale(2, RoundingMode.HALF_UP).toPlainString())
                        .append("%, 盈利").append(win).append("笔\n");
            }
            if (noPlanCount > 0 || violatePlanCount > 0) {
                sb.append("【计划执行情况】无计划随意").append(noPlanCount)
                        .append("笔, 不符合计划").append(violatePlanCount).append("笔\n");
            }
        } catch (Exception e) {
            logger.error("聚合交易记录明细失败", e);
        }
        return sb.toString();
    }

    /**
     * 预测准确率摘要
     */
    private String buildPredictionSummary() {
        StringBuilder sb = new StringBuilder();
        try {
            List<PredictionSimulate> preds = predictionSimulateService
                    .getPredictionByConditionAndPage(1, 100, new PredictionSimulateDto()).getList();
            if (preds == null || preds.isEmpty()) {
                return "";
            }
            int total = 0, correct = 0, wrong = 0;
            for (PredictionSimulate p : preds) {
                if (p.getPredictionResult() != null) {
                    total++;
                    if (p.getPredictionResult() == 1) correct++;
                    else if (p.getPredictionResult() == 2) wrong++;
                }
            }
            sb.append("【预测准确率】共").append(total).append("次预测, 正确").append(correct)
                    .append("次, 错误").append(wrong).append("次");
            if (total > 0) {
                sb.append(", 准确率").append(new BigDecimal(correct * 100.0 / total)
                        .setScale(1, RoundingMode.HALF_UP).toPlainString()).append("%");
            }
            sb.append("\n");
        } catch (Exception e) {
            logger.error("聚合预测数据失败", e);
        }
        return sb.toString();
    }

    /**
     * 资产收支摘要（近3个月月度净额+支出类型分布）—— 反映资金管理习惯
     */
    private String buildAssetSummary() {
        StringBuilder sb = new StringBuilder();
        try {
            Map<String, Object> result = assetTransactionService
                    .getAssetTransactionListByConditionAndPage(1, 1000, new AssetTransactionDto());
            Object dataObj = result == null ? null : result.get("dataList");
            if (!(dataObj instanceof PageInfo)) return "";
            PageInfo<?> pageInfo = (PageInfo<?>) dataObj;
            List<?> rawList = pageInfo.getList();
            if (rawList == null || rawList.isEmpty()) return "";
            // 月度收支
            Map<String, double[]> monthly = new TreeMap<>();
            Map<Integer, Double> spendingTypeAmount = new TreeMap<>();
            SimpleDateFormat mf = new SimpleDateFormat("yyyy-MM");
            for (Object obj : rawList) {
                if (!(obj instanceof AssetTransactionVo)) continue;
                AssetTransactionVo t = (AssetTransactionVo) obj;
                if (t.getRecordTime() == null || t.getAmount() == null || t.getTransactionType() == null) continue;
                String month = mf.format(t.getRecordTime());
                double[] arr = monthly.computeIfAbsent(month, k -> new double[2]);
                if (t.getTransactionType() == 1) arr[0] += t.getAmount().doubleValue();
                else arr[1] += t.getAmount().doubleValue();
                if (t.getTransactionType() == 2 && t.getSpendingType() != null) {
                    spendingTypeAmount.merge(t.getSpendingType(), t.getAmount().doubleValue(), Double::sum);
                }
            }
            if (monthly.isEmpty()) return "";
            sb.append("【资产收支摘要】\n");
            List<String> months = new ArrayList<>(monthly.keySet());
            int start = Math.max(0, months.size() - 3);
            for (int i = start; i < months.size(); i++) {
                String m = months.get(i);
                double[] arr = monthly.get(m);
                sb.append(m).append(": 收入").append(new BigDecimal(arr[0]).setScale(0, RoundingMode.HALF_UP).toPlainString())
                        .append(" 支出").append(new BigDecimal(arr[1]).setScale(0, RoundingMode.HALF_UP).toPlainString())
                        .append(" 净额").append(new BigDecimal(arr[0] - arr[1]).setScale(0, RoundingMode.HALF_UP).toPlainString()).append("\n");
            }
            if (!spendingTypeAmount.isEmpty()) {
                String[] names = {"", "必要", "需要", "想要"};
                sb.append("支出类型分布: ");
                for (Map.Entry<Integer, Double> e : spendingTypeAmount.entrySet()) {
                    int idx = e.getKey();
                    if (idx >= 1 && idx <= 3) {
                        sb.append(names[idx]).append(new BigDecimal(e.getValue()).setScale(0, RoundingMode.HALF_UP).toPlainString()).append(" ");
                    }
                }
                sb.append("\n");
            }
        } catch (Exception e) {
            logger.error("聚合资产收支失败", e);
        }
        return sb.toString();
    }

    /**
     * 情绪温度/心理状态/适配体系分布摘要
     */
    private String buildDistSummary(ReviewReportVo vo) {
        StringBuilder sb = new StringBuilder();
        sb.append("【情绪温度趋势】").append(summarizeTrends(vo.getEmotionTrends(), "score")).append("\n");
        sb.append("【市场状态分布】").append(summarizeStats(vo.getMarketStatusStats())).append("\n");
        sb.append("【心理状态分布】").append(summarizeStats(vo.getPsychologyStats())).append("\n");
        sb.append("【适配体系分布】").append(summarizeStats(vo.getAdaptSystemStats())).append("\n");
        return sb.toString();
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