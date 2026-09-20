package com.xk.srhwzzqdn.manager.assetControlArea.service;

import java.util.Map;

/**
 * 短线选股服务：三类精选（妖股梯队/低位潜伏/趋势延续）各top10 + 行为判定 + 时段归库 + 经验闭环
 */
public interface ShortTermPickService {

    /**
     * 短线精选推荐（主入口）：
     * 非交易日直读库里上个交易日的收盘版；交易日盘中实时算并先删后插intraday版；
     * 交易日收盘后库里已是close版则直读，否则实时算并先删后插close版。
     */
    Map<String, Object> getShortTermStocks();

    /**
     * 查询最近的复盘经验列表（AI数据记忆）
     */
    Map<String, Object> getExperience(Integer limit);

    /**
     * 手动触发复盘：对3~10天前尚未复盘的推荐拉K线验证（T+3/T+5），
     * 事实归类后交AI总结经验入库（AI失败时规则模板兜底）
     */
    Map<String, Object> reviewShortTermPicks();
}
