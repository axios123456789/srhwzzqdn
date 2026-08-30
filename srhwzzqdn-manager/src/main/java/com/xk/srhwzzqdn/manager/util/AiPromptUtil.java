package com.xk.srhwzzqdn.manager.util;

import java.math.BigDecimal;

/**
 * AI Prompt 构建工具类
 * 封装各场景的 system prompt 和 user prompt 模板
 */
public class AiPromptUtil {

    // ==================== P0-1: 每日复盘 AI 生成总结 ====================

    public static final String DAILY_REVIEW_SYSTEM = "你是一位专业的A股交易复盘助手，根据用户提供的当日市场数据，生成复盘总结。" +
            "要求返回纯JSON格式（不要用markdown代码块包裹），包含以下字段：\n" +
            "{\n" +
            "  \"experience\": \"经验总结（150字内，总结当日市场特征和个人操作得失）\",\n" +
            "  \"lesson\": \"教训反思（150字内，反思不足之处）\",\n" +
            "  \"improvePoint\": \"改进点（100字内，给出具体可执行的改进措施）\",\n" +
            "  \"tomorrowFocus\": \"明日关注重点（100字内，提示次日需要关注的方向）\"\n" +
            "}";

    public static String buildDailyReviewPrompt(Object[] fields) {
        return String.format(
                "请根据以下当日复盘数据生成总结：\n" +
                        "复盘日期：%s\n" +
                        "【大盘环境】上证涨跌：%s%%，深证涨跌：%s%%，创业板涨跌：%s%%\n" +
                        "两市成交额：%s亿，上涨%s家/下跌%s家\n" +
                        "市场状态：%s\n" +
                        "【情绪指标】涨停%s家，跌停%s家，连板%s家，炸板%s家，炸板率%s%%\n" +
                        "昨日涨停溢价：%s%%，情绪温度：%s\n" +
                        "北向资金：%s万\n" +
                        "【主线板块】%s\n" +
                        "龙头股：%s（%s连板）\n" +
                        "【次日策略】适配体系：%s，计划仓位上限：%s%%\n" +
                        "止损线：%s%%，止盈线：%s%%\n" +
                        "【持仓自评】当日盈亏：%s%%，操作自评：%s\n" +
                        "交易次数：%s，盈利次数：%s\n" +
                        "今日操作记录：%s",
                fields);
    }

    // ==================== P0-2: 复盘分析 AI 报告 ====================

    public static final String REVIEW_REPORT_SYSTEM = "你是一位专业的交易分析师，根据用户提供的交易统计数据，生成深度分析报告。" +
            "要求返回Markdown格式，包含以下章节：\n" +
            "## 一、整体绩效评价\n" +
            "## 二、情绪与纪律分析\n" +
            "## 三、交易模式诊断\n" +
            "## 四、风险控制评估\n" +
            "## 五、改进建议\n\n" +
            "每个章节2-3段，改进建议给出3条具体可执行措施。总字数控制在800-1200字。";

    public static String buildReviewReportPrompt(int rangeDays, Object[] kpis) {
        return String.format(
                "请根据以下近%s天的交易统计数据生成深度分析报告：\n\n" +
                        "【复盘KPI】\n总复盘天数：%s\n盈利天数：%s，亏损天数：%s\n" +
                        "最高连续盈利：%s天\n平均每日盈亏：%s%%\n\n" +
                        "【交易KPI】\n总交易笔数：%s\n盈利笔数：%s，亏损笔数：%s\n" +
                        "胜率：%s%%\n平均盈亏：%s%%\n最大单笔盈利：%s%%\n最大单笔亏损：%s%%\n\n" +
                        "【情绪温度趋势】%s\n\n" +
                        "【市场状态分布】%s\n\n" +
                        "【心理状态分布】%s\n\n" +
                        "【操作自评趋势】%s\n\n" +
                        "【执行评分趋势】%s\n\n" +
                        "【适配体系分布】%s",
                rangeDays, kpis);
    }

    // ==================== P0-3: 关注标的 AI 分析 ====================

    public static final String TARGET_ANALYSIS_SYSTEM = "你是一位专业的A股投资分析师，根据用户提供的关注标的和当日市场环境，" +
            "结合基本面、技术面、资金面进行综合分析，生成买入条件和风险预警。\n" +
            "要求返回纯JSON格式（不要用markdown代码块包裹）：\n" +
            "{\n" +
            "  \"buyCondition\": \"买入条件（200字内，针对关注标的给出具体的买入触发条件，结合技术指标、量价关系、板块联动等）\",\n" +
            "  \"riskWarning\": \"风险预警（200字内，提示关键风险点，包括大盘风险、个股风险、资金风险等）\"\n" +
            "}";

    public static String buildTargetAnalysisPrompt(Object[] fields) {
        return String.format(
                "请根据以下信息分析关注标的，生成买入条件和风险预警：\n" +
                        "【关注标的】%s\n" +
                        "【当日市场环境】\n" +
                        "市场状态：%s，情绪温度：%s\n" +
                        "涨停%s家，跌停%s家，连板%s家，炸板率%s%%\n" +
                        "北向资金：%s万，两市成交额：%s亿\n" +
                        "主线板块：%s\n" +
                        "适配体系：%s，计划仓位上限：%s%%\n" +
                        "止损线：%s%%，止盈线：%s%%\n" +
                        "【复盘日期】%s\n\n" +
                        "请结合你掌握的这些标的的基本面（行业地位、业绩预期）、技术面（近期走势、关键支撑压力位）、" +
                        "资金面（主力资金动向、北向资金偏好）进行综合分析。",
                fields);
    }

    // ==================== 辅助方法 ====================

    // ==================== P0-4: 智能预测 AI 自动填充 ====================

    public static final String PREDICTION_SYSTEM = "你是一位专业的A股投资分析师，根据用户提供的股票名称和代码，" +
            "结合基本面（行业地位、业绩预期）、技术面（近期走势、量价关系）、资金面（主力资金动向）进行综合分析，" +
            "预测该股票次日涨跌趋势。\n" +
            "要求返回纯JSON格式（不要用markdown代码块包裹），包含以下字段：\n" +
            "{\n" +
            "  \"riseFallPrediction\": 1或2（1-涨 2-跌），\n" +
            "  \"basisType\": \"依据类型码值逗号分隔（1-技术分析 2-基本面分析 3-消息面分析 4-逻辑分析 5-情绪分析 6-资金面分析），可多选\",\n" +
            "  \"predictionContent\": \"预测内容（50字内，描述次日涨跌预期和幅度）\",\n" +
            "  \"predictionBasis\": \"预测依据（150字内，详细分析理由）\"\n" +
            "}";

    public static String buildPredictionPrompt(Object[] fields) {
        return String.format(
                "请根据以下信息预测该股票次日涨跌趋势：\n" +
                        "【股票信息】股票名称：%s，股票代码：%s\n" +
                        "【历史预测参考】\n%s\n" +
                        "请结合你掌握的该股票的基本面、技术面、资金面进行综合分析，给出预测。",
                fields);
    }

    public static String safeStr(Object obj) {
        if (obj == null) return "未填写";
        String s = obj.toString().trim();
        return s.isEmpty() ? "未填写" : s;
    }

    public static String safeNum(BigDecimal num) {
        if (num == null) return "0";
        return num.toPlainString();
    }

    public static String safeNum(Integer num) {
        if (num == null) return "0";
        return num.toString();
    }
}