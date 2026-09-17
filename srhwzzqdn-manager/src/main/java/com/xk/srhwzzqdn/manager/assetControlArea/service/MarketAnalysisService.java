package com.xk.srhwzzqdn.manager.assetControlArea.service;

import com.xk.srhwzzqdn.model.entity.assetControl.MarketAnalysisDaily;

import java.util.List;
import java.util.Map;

/**
 * 市场实时分析服务：聚合东方财富市场级接口，实时回答
 * 市场在做什么/情绪、消息面、资金流向、增量退场、主线板块与龙头、持续性判断等问题
 */
public interface MarketAnalysisService {

    /**
     * 实时市场聚合分析（规则引擎，不调AI不入库）：竞价时段取竞价数据、盘中取实时数据
     */
    Map<String, Object> getRealtimeAnalysis();

    /**
     * AI深度分析：实时聚合 + AI多维分析（市场状态/消息逻辑/资金/主线持续性/明日展望/历史复盘）
     * 并按天落库（当天已有记录则先删当天再插入，不触碰其他交易日），返回完整结果
     */
    Map<String, Object> analyzeWithAi();

    /**
     * 查询当天已落库的最新市场分析（无则返回null）
     */
    MarketAnalysisDaily getTodayAnalysis();

    /**
     * 查询最近N天的历史分析列表（轻量字段，供历史记录表格展示）
     */
    List<Map<String, Object>> getHistoryList(Integer limit);

    /**
     * AI历史对比复盘：用库内最近若干天的分析记录与AI展望，对比实际走势验证准确性并总结教训
     */
    Map<String, Object> getHistoryReview();
}
