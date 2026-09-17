package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.xk.srhwzzqdn.manager.assetControlArea.service.MarketAnalysisService;
import com.xk.srhwzzqdn.model.entity.assetControl.MarketAnalysisDaily;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 市场实时分析控制器：聚合东方财富市场级数据（指数/涨跌家数/涨跌停池/板块资金流/快讯/两融），
 * 实时回答市场情绪、消息面、资金流向、主线板块与龙头等问题；AI分析结果按天落库
 */
@RestController
@RequestMapping("/superBrain/assetControl/marketAnalysis")
public class MarketAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(MarketAnalysisController.class);

    @Autowired
    private MarketAnalysisService marketAnalysisService;

    /**
     * 实时市场聚合分析（规则引擎，不调AI不入库）：竞价时段取竞价数据、盘中取实时数据
     */
    @GetMapping("/getRealtimeAnalysis")
    public Result getRealtimeAnalysis() {
        try {
            Map<String, Object> result = marketAnalysisService.getRealtimeAnalysis();
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("市场实时分析失败", e);
            return Result.build(null, 500, "市场实时分析失败：" + e.getMessage());
        }
    }

    /**
     * AI深度分析：实时聚合 + AI多维分析 + 按天落库（当天已有记录先删当天再插入）
     */
    @GetMapping("/analyzeWithAi")
    public Result analyzeWithAi() {
        try {
            Map<String, Object> result = marketAnalysisService.analyzeWithAi();
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("市场AI深度分析失败", e);
            return Result.build(null, 500, "市场AI深度分析失败：" + e.getMessage());
        }
    }

    /**
     * 查询当天已落库的最新市场分析（页面首屏快速恢复）
     */
    @GetMapping("/getTodayAnalysis")
    public Result getTodayAnalysis() {
        try {
            MarketAnalysisDaily today = marketAnalysisService.getTodayAnalysis();
            return Result.build(today, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("查询当日市场分析失败", e);
            return Result.build(null, 500, "查询当日市场分析失败：" + e.getMessage());
        }
    }

    /**
     * 历史分析列表（最近N天轻量摘要）
     */
    @GetMapping("/getHistoryList/{limit}")
    public Result getHistoryList(@PathVariable Integer limit) {
        try {
            List<Map<String, Object>> list = marketAnalysisService.getHistoryList(limit);
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("查询历史分析失败", e);
            return Result.build(null, 500, "查询历史分析失败：" + e.getMessage());
        }
    }

    /**
     * AI历史对比复盘：历史展望 vs 实际走势，验证分析准确性并总结纠错规则
     */
    @GetMapping("/getHistoryReview")
    public Result getHistoryReview() {
        try {
            Map<String, Object> result = marketAnalysisService.getHistoryReview();
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("AI历史复盘失败", e);
            return Result.build(null, 500, "AI历史复盘失败：" + e.getMessage());
        }
    }
}
