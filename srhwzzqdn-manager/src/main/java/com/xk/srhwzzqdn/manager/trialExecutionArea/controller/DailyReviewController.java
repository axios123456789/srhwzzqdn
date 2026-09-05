package com.xk.srhwzzqdn.manager.trialExecutionArea.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.DailyReviewService;
import com.xk.srhwzzqdn.manager.util.AiCommonUtil;
import com.xk.srhwzzqdn.manager.util.AiPromptUtil;
import com.xk.srhwzzqdn.manager.util.StockQuoteUtil;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.DailyReviewDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.DailyReview;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superBrain/trialExecution/dailyReview")
public class DailyReviewController {
    private static final Logger logger = LoggerFactory.getLogger(DailyReviewController.class);

    @Autowired
    private DailyReviewService dailyReviewService;

    @Autowired
    private AiCommonUtil aiCommonUtil;

    /**
     * 条件分页查询每日复盘列表
     */
    @PostMapping("/getDailyReviewByConditionAndPage/{current}/{limit}")
    public Result getDailyReviewByConditionAndPage(@PathVariable("current") Integer current,
                                                    @PathVariable("limit") Integer limit,
                                                    @RequestBody DailyReviewDto dto) {
        PageInfo<DailyReview> pageInfo = dailyReviewService.getDailyReviewByConditionAndPage(current, limit, dto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存每日复盘（同日期自动覆盖更新）
     */
    @PostMapping("/saveDailyReview")
    public Result saveDailyReview(@RequestBody DailyReview dailyReview) {
        try {
            dailyReviewService.saveDailyReview(dailyReview);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存每日复盘失败！");
        }
    }

    /**
     * 根据id删除每日复盘
     */
    @DeleteMapping("/deleteDailyReviewById/{id}")
    public Result deleteDailyReviewById(@PathVariable("id") Long id) {
        try {
            dailyReviewService.deleteDailyReviewById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除每日复盘失败！");
        }
    }

    /**
     * 根据ids批量删除每日复盘
     */
    @PostMapping("/deleteAllDailyReviewByIds")
    public Result deleteAllDailyReviewByIds(@RequestBody List<Long> ids) {
        try {
            dailyReviewService.deleteAllDailyReviewByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除每日复盘失败！");
        }
    }

    /**
     * AI生成复盘总结（经验总结/教训反思/改进点/明日关注）
     * 前端传入复盘表单数据 + 字典文本，返回AI生成的4个字段
     */
    @PostMapping("/aiGenerate")
    public Result aiGenerate(@RequestBody Map<String, Object> formData) {
        try {
            String prompt = AiPromptUtil.buildDailyReviewPrompt(new Object[]{
                    AiPromptUtil.safeStr(formData.get("reviewDate")),
                    AiPromptUtil.safeStr(formData.get("shChangePct")),
                    AiPromptUtil.safeStr(formData.get("szChangePct")),
                    AiPromptUtil.safeStr(formData.get("cybChangePct")),
                    AiPromptUtil.safeStr(formData.get("totalAmount")),
                    AiPromptUtil.safeStr(formData.get("riseCount")),
                    AiPromptUtil.safeStr(formData.get("fallCount")),
                    AiPromptUtil.safeStr(formData.get("marketStatusText")),
                    AiPromptUtil.safeStr(formData.get("limitUpCount")),
                    AiPromptUtil.safeStr(formData.get("limitDownCount")),
                    AiPromptUtil.safeStr(formData.get("continuousBoardCount")),
                    AiPromptUtil.safeStr(formData.get("brokenBoardCount")),
                    AiPromptUtil.safeStr(formData.get("brokenBoardRate")),
                    AiPromptUtil.safeStr(formData.get("yesterdayPremiumPct")),
                    AiPromptUtil.safeStr(formData.get("emotionTempText")),
                    AiPromptUtil.safeStr(formData.get("northFlowAmount")),
                    AiPromptUtil.safeStr(formData.get("sectorText")),
                    AiPromptUtil.safeStr(formData.get("leaderStockName")),
                    AiPromptUtil.safeStr(formData.get("leaderContinuousBoard")),
                    AiPromptUtil.safeStr(formData.get("adaptSystemText")),
                    AiPromptUtil.safeStr(formData.get("planPositionLimit")),
                    AiPromptUtil.safeStr(formData.get("stopLossPct")),
                    AiPromptUtil.safeStr(formData.get("takeProfitPct")),
                    AiPromptUtil.safeStr(formData.get("dailyProfitPct")),
                    AiPromptUtil.safeStr(formData.get("selfRatingText")),
                    AiPromptUtil.safeStr(formData.get("tradeCount")),
                    AiPromptUtil.safeStr(formData.get("winTradeCount")),
                    AiPromptUtil.safeStr(formData.get("todayOperation"))
            });

            String aiResult = aiCommonUtil.callWithSystem(AiPromptUtil.DAILY_REVIEW_SYSTEM, prompt);
            if (aiResult == null || aiResult.trim().isEmpty()) {
                return Result.build(null, 500, "AI生成失败，请检查AI配置");
            }

            // 尝试解析JSON，如果AI返回了markdown代码块则提取
            String jsonStr = extractJson(aiResult);
            JSONObject json = JSON.parseObject(jsonStr);

            JSONObject result = new JSONObject();
            result.put("experience", json.getString("experience"));
            result.put("lesson", json.getString("lesson"));
            result.put("improvePoint", json.getString("improvePoint"));
            result.put("tomorrowFocus", json.getString("tomorrowFocus"));
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("AI生成复盘总结失败", e);
            return Result.build(null, 500, "AI生成失败：" + e.getMessage());
        }
    }

    /**
     * AI分析关注标的，生成买入条件和风险预警
     * 根据关注标的+当日市场环境，AI综合基本面/技术面/资金面进行分析
     */
    @PostMapping("/aiAnalyzeTargets")
    public Result aiAnalyzeTargets(@RequestBody Map<String, Object> formData) {
        try {
            String watchTargets = AiPromptUtil.safeStr(formData.get("watchTargets"));
            if ("未填写".equals(watchTargets)) {
                return Result.build(null, 400, "请先填写关注标的");
            }

            String prompt = AiPromptUtil.buildTargetAnalysisPrompt(new Object[]{
                    watchTargets,
                    AiPromptUtil.safeStr(formData.get("marketStatusText")),
                    AiPromptUtil.safeStr(formData.get("emotionTempText")),
                    AiPromptUtil.safeStr(formData.get("limitUpCount")),
                    AiPromptUtil.safeStr(formData.get("limitDownCount")),
                    AiPromptUtil.safeStr(formData.get("continuousBoardCount")),
                    AiPromptUtil.safeStr(formData.get("brokenBoardRate")),
                    AiPromptUtil.safeStr(formData.get("northFlowAmount")),
                    AiPromptUtil.safeStr(formData.get("totalAmount")),
                    AiPromptUtil.safeStr(formData.get("sectorText")),
                    AiPromptUtil.safeStr(formData.get("adaptSystemText")),
                    AiPromptUtil.safeStr(formData.get("planPositionLimit")),
                    AiPromptUtil.safeStr(formData.get("stopLossPct")),
                    AiPromptUtil.safeStr(formData.get("takeProfitPct")),
                    AiPromptUtil.safeStr(formData.get("reviewDate"))
            });

            String aiResult = aiCommonUtil.callWithSystem(AiPromptUtil.TARGET_ANALYSIS_SYSTEM, prompt);
            if (aiResult == null || aiResult.trim().isEmpty()) {
                return Result.build(null, 500, "AI分析失败，请检查AI配置");
            }

            String jsonStr = extractJson(aiResult);
            JSONObject json = JSON.parseObject(jsonStr);

            JSONObject result = new JSONObject();
            result.put("buyCondition", json.getString("buyCondition"));
            result.put("riskWarning", json.getString("riskWarning"));
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("AI分析关注标的失败", e);
            return Result.build(null, 500, "AI分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取市场数据（3大指数涨跌幅+两市成交额+涨跌家数统计）
     * 不传date：获取当天实时数据；传date（yyyy-MM-dd）：获取复盘日期对应交易日的数据
     * 用于每日复盘表单自动填充
     */
    @GetMapping("/fetchRealtimeMarketData")
    public Result fetchRealtimeMarketData(@RequestParam(value = "date", required = false) String date) {
        try {
            JSONObject data;
            if (date == null || date.trim().isEmpty()) {
                data = StockQuoteUtil.getMarketOverview();
            } else {
                data = StockQuoteUtil.getMarketOverviewByDate(date.trim());
            }
            return Result.build(data, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取大盘数据失败", e);
            return Result.build(null, 500, "获取市场数据失败：" + e.getMessage());
        }
    }

    /**
     * 从AI响应中提取JSON字符串（处理markdown代码块包裹的情况）
     */
    private String extractJson(String aiResult) {
        String trimmed = aiResult.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}