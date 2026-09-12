package com.xk.srhwzzqdn.manager.trialExecutionArea.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.assetControlArea.service.StockAssetService;
import com.xk.srhwzzqdn.manager.trialExecutionArea.mapper.TransactionSystemTrialMapper;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.TransactionSystemTrialService;
import com.xk.srhwzzqdn.manager.util.AiCommonUtil;
import com.xk.srhwzzqdn.manager.util.AiPromptUtil;
import com.xk.srhwzzqdn.manager.util.StockQuoteUtil;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.AutoGeneratePlanDto;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TransactionSystemTrialDto;
import com.xk.srhwzzqdn.model.entity.assetControl.StockBasic;
import com.xk.srhwzzqdn.model.entity.assetControl.StockFinance;
import com.xk.srhwzzqdn.model.entity.assetControl.StockNews;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionSystemTrial;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionRule;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionSystemTrialServiceImpl implements TransactionSystemTrialService {
    @Autowired
    private TransactionSystemTrialMapper transactionSystemTrialMapper;

    @Autowired
    private StockAssetService stockAssetService;

    @Autowired
    private AiCommonUtil aiCommonUtil;

    /**
     * 条件分页查询交易系统试验列表
     * @param current
     * @param limit
     * @param dto
     * @return
     */
    @Override
    public PageInfo<TransactionSystemTrial> getTransactionSystemTrialByConditionAndPage(Integer current, Integer limit, TransactionSystemTrialDto dto) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        //2.设置查询条件（所属人）
        dto.setOwner(AuthContextUtil.get().getId());

        //3.条件查询交易系统试验列表
        List<TransactionSystemTrial> list = transactionSystemTrialMapper.getTransactionSystemTrialByCondition(dto);

        //4.设置分页
        PageInfo<TransactionSystemTrial> pageInfo = new PageInfo<>(list);

        return pageInfo;
    }

    /**
     * 保存交易系统试验（含规则次数联动）
     * @param transactionSystemTrial
     */
    @Override
    public void saveTransactionSystemTrial(TransactionSystemTrial transactionSystemTrial) {
        if (transactionSystemTrial.getId() == null) {//添加
            transactionSystemTrial.setOwner(AuthContextUtil.get().getId());
            transactionSystemTrialMapper.addTransactionSystemTrial(transactionSystemTrial);
            // 新增时追加规则次数
            if (transactionSystemTrial.getIsUsePlan() != null && transactionSystemTrial.getIsUsePlan() == 1) {
                addRuleCounts(transactionSystemTrial);
            }
        } else {//修改
            // 先查询旧记录用于回退
            TransactionSystemTrial oldRecord = transactionSystemTrialMapper.getTransactionSystemTrialById(transactionSystemTrial.getId());
            transactionSystemTrialMapper.updateTransactionSystemTrial(transactionSystemTrial);
            // 修改时：先回退旧记录的规则次数，再追加新记录的规则次数
            if (oldRecord != null) {
                rollbackRuleCounts(oldRecord);
            }
            if (transactionSystemTrial.getIsUsePlan() != null && transactionSystemTrial.getIsUsePlan() == 1) {
                addRuleCounts(transactionSystemTrial);
            }
        }
    }

    /**
     * 自动生成交易计划
     * 优化：直接获取原始数据（不调analyzeStock省1次AI）+ 合并AI调用（1次生成计划价/内容/规则匹配）+ 结合历史交易数据
     */
    @Override
    public TransactionSystemTrial autoGenerateTradePlan(AutoGeneratePlanDto dto) {
        String stockCode = dto.getStockCode();
        Integer tradeType = dto.getTradeType();
        Integer planType = dto.getPlanType();

        // 1. 获取股票基本信息（股票名称、当前价）
        StockBasic basic = stockAssetService.getStockBasicByCode(stockCode);
        String stockName = basic != null && basic.getStockName() != null ? basic.getStockName() : stockCode;
        BigDecimal currentPrice = basic != null ? basic.getLastPrice() : null;

        // 2. 获取实时行情文本（报价+30日K线+5日资金流向）
        String realtimeData;
        try {
            realtimeData = StockQuoteUtil.getRealtimeMarketData(stockCode);
        } catch (Exception e) {
            realtimeData = "实时行情获取失败：" + e.getMessage();
        }

        // 3. 获取消息面文本
        String newsText;
        try {
            List<StockNews> newsList = stockAssetService.getStockNews(stockCode, 15);
            newsText = buildNewsText(newsList);
        } catch (Exception e) {
            newsText = "消息面获取失败：" + e.getMessage();
        }

        // 4. 获取基本面文本
        String financeText;
        try {
            List<StockFinance> financeList = stockAssetService.getStockFinance(stockCode, 8);
            financeText = buildFinanceText(financeList);
        } catch (Exception e) {
            financeText = "基本面获取失败：" + e.getMessage();
        }

        // 5. 获取该股票历史交易数据（每日交易记录）
        String historyTradeText = buildHistoryTradeText(stockName);

        // 6. 获取当前用户交易规则列表
        List<TransactionRule> ruleList = getTransactionRuleList();
        String rulesText = buildRulesText(ruleList);

        // 7. AI分析（单次调用生成计划价/计划内容/匹配规则）
        BigDecimal planPrice = currentPrice;
        String planContent = "AI生成计划内容失败，请手动编辑";
        String complyRuleIds = "";
        try {
            String prompt = AiPromptUtil.buildGeneratePlanPrompt(stockCode, stockName, currentPrice, tradeType, planType,
                    realtimeData, newsText, financeText, historyTradeText, rulesText);
            String aiResult = aiCommonUtil.callWithSystem(AiPromptUtil.GENERATE_PLAN_SYSTEM, prompt);
            String jsonStr = extractJson(aiResult);
            if (jsonStr != null) {
                JSONObject json = JSON.parseObject(jsonStr);
                if (json.containsKey("planPrice") && json.get("planPrice") != null) {
                    planPrice = json.getBigDecimal("planPrice");
                }
                if (json.containsKey("planContent") && json.getString("planContent") != null) {
                    planContent = json.getString("planContent");
                }
                JSONArray ids = json.getJSONArray("complyRuleIds");
                if (ids != null && !ids.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < ids.size(); i++) {
                        if (i > 0) sb.append(",");
                        sb.append(ids.getInteger(i));
                    }
                    complyRuleIds = sb.toString();
                }
            }
        } catch (Exception e) {
            // 保持兜底值
        }

        // 8. 构造交易试验记录并设置默认字段（同添加试验模态窗默认值）
        TransactionSystemTrial trial = new TransactionSystemTrial();
        trial.setTradeType(tradeType);
        trial.setTargetName(stockName);
        trial.setPlanType(planType);
        trial.setPlanContent(planContent);
        trial.setCurrentPrice(currentPrice);
        trial.setPlanPrice(planPrice);
        trial.setTradeStatus(1);
        trial.setComplyRuleIds(complyRuleIds);
        trial.setIsUsePlan(0);
        String[] nextTradingTime = getNextTradingTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            trial.setPlanStartTime(sdf.parse(nextTradingTime[0]));
            trial.setPlanEndTime(sdf.parse(nextTradingTime[1]));
        } catch (Exception ignored) {
        }

        // 9. 入库（saveTransactionSystemTrial 会设置 owner）
        saveTransactionSystemTrial(trial);

        return trial;
    }

    /**
     * 构建消息面文本
     */
    private String buildNewsText(List<StockNews> newsList) {
        if (newsList == null || newsList.isEmpty()) return "无消息面数据";
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (StockNews news : newsList) {
            String typeDesc = news.getNewsType() != null && news.getNewsType() == 2 ? "公告" : "新闻";
            sb.append("[").append(typeDesc).append("]")
                    .append(news.getPublishTime() != null ? sdf.format(news.getPublishTime()) : "未知日期")
                    .append(" ").append(news.getTitle() != null ? news.getTitle() : "无标题");
            if (news.getSummary() != null && !news.getSummary().trim().isEmpty()) {
                String summary = news.getSummary().length() > 80 ? news.getSummary().substring(0, 80) + "..." : news.getSummary();
                sb.append(" | 摘要:").append(summary);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 构建基本面文本
     */
    private String buildFinanceText(List<StockFinance> financeList) {
        if (financeList == null || financeList.isEmpty()) return "无基本面数据";
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (StockFinance f : financeList) {
            sb.append("报告期:").append(f.getReportDate() != null ? sdf.format(f.getReportDate()) : "未知")
                    .append(" | 营收:").append(f.getRevenue() != null ? f.getRevenue().toPlainString() : "无").append("亿")
                    .append(" | 营收同比:").append(f.getRevenueYoy() != null ? f.getRevenueYoy().toPlainString() + "%" : "无")
                    .append(" | 净利润:").append(f.getNetProfit() != null ? f.getNetProfit().toPlainString() : "无").append("亿")
                    .append(" | 净利润同比:").append(f.getNetProfitYoy() != null ? f.getNetProfitYoy().toPlainString() + "%" : "无")
                    .append(" | 毛利率:").append(f.getGrossMargin() != null ? f.getGrossMargin().toPlainString() + "%" : "无")
                    .append(" | ROE:").append(f.getRoe() != null ? f.getRoe().toPlainString() + "%" : "无")
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * 查询该股票历史交易记录并构建文本（供AI结合每日交易数据制定计划）
     */
    private String buildHistoryTradeText(String stockName) {
        try {
            TransactionSystemTrialDto queryDto = new TransactionSystemTrialDto();
            queryDto.setTargetName(stockName);
            queryDto.setOwner(AuthContextUtil.get().getId());
            List<TransactionSystemTrial> list = transactionSystemTrialMapper.getTransactionSystemTrialByCondition(queryDto);
            if (list == null || list.isEmpty()) return "无历史交易记录";
            int limit = Math.min(list.size(), 20);
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < limit; i++) {
                TransactionSystemTrial t = list.get(i);
                String tradeTypeDesc = t.getTradeType() == 1 ? "买入" : "卖出";
                String planTypeDesc = t.getPlanType() == 1 ? "止盈" : t.getPlanType() == 2 ? "止损" : "建仓";
                String statusDesc = t.getTradeStatus() == 1 ? "未开始" : t.getTradeStatus() == 2 ? "进行中" : "已结束";
                String resultDesc = t.getTradeResult() == null ? "未出结果" : t.getTradeResult() == 1 ? "成功" : "失败";
                sb.append("时间:").append(t.getCreateTime() != null ? sdf.format(t.getCreateTime()) : "未知")
                        .append(" | 类型:").append(tradeTypeDesc).append(planTypeDesc)
                        .append(" | 计划价:").append(t.getPlanPrice() != null ? t.getPlanPrice().toPlainString() : "无")
                        .append(" | 成交价:").append(t.getActualPrice() != null ? t.getActualPrice().toPlainString() : "无")
                        .append(" | 状态:").append(statusDesc)
                        .append(" | 结果:").append(resultDesc);
                if (t.getComplyRuleIds() != null && !t.getComplyRuleIds().isEmpty()) {
                    sb.append(" | 遵守规则:").append(t.getComplyRuleIds());
                }
                if (t.getResultReview() != null && !t.getResultReview().isEmpty()) {
                    String review = t.getResultReview().length() > 50 ? t.getResultReview().substring(0, 50) : t.getResultReview();
                    sb.append(" | 复盘:").append(review);
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "历史交易数据获取失败：" + e.getMessage();
        }
    }

    /**
     * 从AI返回文本中提取JSON字符串（兼容markdown代码块包裹）
     */
    private String extractJson(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        text = text.trim();
        if (text.startsWith("```")) {
            text = text.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
        }
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    /**
     * 构建规则列表文本供AI匹配
     */
    private String buildRulesText(List<TransactionRule> ruleList) {
        if (ruleList == null || ruleList.isEmpty()) return "无可用规则";
        StringBuilder sb = new StringBuilder();
        for (TransactionRule rule : ruleList) {
            String ruleTypeDesc = rule.getRuleType() == 1 ? "思想规则" : rule.getRuleType() == 2 ? "选股规则" :
                    rule.getRuleType() == 3 ? "入场规则" : rule.getRuleType() == 4 ? "止损规则" :
                    rule.getRuleType() == 5 ? "止盈规则" : "仓位规则";
            sb.append("ID:").append(rule.getId())
                    .append(" | 规则编号:").append(rule.getRuleCode())
                    .append(" | 类型:").append(ruleTypeDesc)
                    .append(" | 内容:").append(rule.getRuleContent());
            if (rule.getRuleDetail() != null && !rule.getRuleDetail().trim().isEmpty()) {
                sb.append(" | 细节:").append(rule.getRuleDetail());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 计算下一交易日的开盘和收盘时间（A股 9:30-15:00，跳过周末）
     */
    private String[] getNextTradingTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY) cal.add(Calendar.DAY_OF_MONTH, 1);
        if (dayOfWeek == Calendar.SATURDAY) cal.add(Calendar.DAY_OF_MONTH, 2);
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dateSdf.format(cal.getTime());
        return new String[]{dateStr + " 09:30:00", dateStr + " 15:00:00"};
    }

    /**
     * 根据id删除交易系统试验（含规则次数回退）
     * @param id
     */
    @Override
    public void deleteTransactionSystemTrialById(Long id) {
        TransactionSystemTrial record = transactionSystemTrialMapper.getTransactionSystemTrialById(id);
        transactionSystemTrialMapper.deleteTransactionSystemTrialById(id);
        if (record != null) {
            rollbackRuleCounts(record);
        }
    }

    /**
     * 根据ids批量删除交易系统试验（含规则次数回退）
     * @param ids
     */
    @Override
    public void deleteAllTransactionSystemTrialByIds(List<Long> ids) {
        for (Long id : ids) {
            TransactionSystemTrial record = transactionSystemTrialMapper.getTransactionSystemTrialById(id);
            if (record != null) {
                rollbackRuleCounts(record);
            }
        }
        transactionSystemTrialMapper.deleteAllTransactionSystemTrialByIds(ids);
    }

    //====================交易规则====================
    /**
     * 查询交易规则列表
     * @return
     */
    @Override
    public List<TransactionRule> getTransactionRuleList() {
        String owner = AuthContextUtil.get().getId();
        return transactionSystemTrialMapper.getTransactionRuleByCondition(owner);
    }

    /**
     * 保存交易规则
     * @param transactionRule
     */
    @Override
    public void saveTransactionRule(TransactionRule transactionRule) {
        if (transactionRule.getId() == null) {//添加
            transactionRule.setOwner(AuthContextUtil.get().getId());
            transactionSystemTrialMapper.addTransactionRule(transactionRule);
        } else {//修改
            transactionSystemTrialMapper.updateTransactionRule(transactionRule);
        }
    }

    /**
     * 根据id删除交易规则
     * @param id
     */
    @Override
    public void deleteTransactionRuleById(Integer id) {
        transactionSystemTrialMapper.deleteTransactionRuleById(id);
    }

    /**
     * 根据ids批量删除交易规则
     * @param ids
     */
    @Override
    public void deleteAllTransactionRuleByIds(List<Integer> ids) {
        transactionSystemTrialMapper.deleteAllTransactionRuleByIds(ids);
    }

    //====================规则次数联动辅助方法====================

    /**
     * 解析逗号分隔的规则ID字符串为Integer列表
     */
    private List<Integer> parseRuleIds(String ruleIdsStr) {
        if (!StringUtils.hasText(ruleIdsStr)) {
            return new ArrayList<>();
        }
        return Arrays.stream(ruleIdsStr.split(","))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * 追加规则次数（新增或修改后的新状态）
     * 联动逻辑：
     * - 第一层：仅当 isUsePlan=1 时才启动统计
     * - 第二层：tradeResult=1(成功)时增加成功次数
     * - 每个规则 useCount 只加1次（去重），无论同时出现在遵守和违反列表中
     * - 遵守规则：成功时 complySuccessCount+1
     * - 违反规则：violateCount+1，成功时 violateSuccessCount+1
     */
    private void addRuleCounts(TransactionSystemTrial record) {
        boolean isSuccess = record.getTradeResult() != null && record.getTradeResult() == 1;

        List<Integer> complyIds = parseRuleIds(record.getComplyRuleIds());
        List<Integer> violateIds = parseRuleIds(record.getViolateRuleIds());

        // 合并去重所有规则ID，useCount每个规则只加1次
        java.util.Set<Integer> allRuleIds = new java.util.LinkedHashSet<>();
        allRuleIds.addAll(complyIds);
        allRuleIds.addAll(violateIds);

        // 先统一处理 useCount（去重后每个规则只加1次）
        for (Integer ruleId : allRuleIds) {
            TransactionRule rule = transactionSystemTrialMapper.getTransactionRuleById(ruleId);
            if (rule != null) {
                rule.setUseCount(rule.getUseCount() != null ? rule.getUseCount() + 1 : 1);
                transactionSystemTrialMapper.updateTransactionRule(rule);
            }
        }

        // 处理遵守规则的成功次数
        for (Integer ruleId : complyIds) {
            if (isSuccess) {
                TransactionRule rule = transactionSystemTrialMapper.getTransactionRuleById(ruleId);
                if (rule != null) {
                    rule.setComplySuccessCount(rule.getComplySuccessCount() != null ? rule.getComplySuccessCount() + 1 : 1);
                    transactionSystemTrialMapper.updateTransactionRule(rule);
                }
            }
        }

        // 处理违反规则的违反次数和成功次数
        for (Integer ruleId : violateIds) {
            TransactionRule rule = transactionSystemTrialMapper.getTransactionRuleById(ruleId);
            if (rule != null) {
                rule.setViolateCount(rule.getViolateCount() != null ? rule.getViolateCount() + 1 : 1);
                if (isSuccess) {
                    rule.setViolateSuccessCount(rule.getViolateSuccessCount() != null ? rule.getViolateSuccessCount() + 1 : 1);
                }
                transactionSystemTrialMapper.updateTransactionRule(rule);
            }
        }
    }

    /**
     * 回退规则次数（修改时回退旧记录、删除时回退）
     * 联动逻辑与addRuleCounts相反：
     * - 仅当 isUsePlan=1 时才回退统计
     * - 每个规则 useCount 只减1次（去重），无论同时出现在遵守和违反列表中
     * - 遵守规则：成功时 complySuccessCount-1
     * - 违反规则：violateCount-1，成功时 violateSuccessCount-1
     */
    private void rollbackRuleCounts(TransactionSystemTrial record) {
        if (record.getIsUsePlan() == null || record.getIsUsePlan() != 1) {
            return;
        }
        boolean isSuccess = record.getTradeResult() != null && record.getTradeResult() == 1;

        List<Integer> complyIds = parseRuleIds(record.getComplyRuleIds());
        List<Integer> violateIds = parseRuleIds(record.getViolateRuleIds());

        // 合并去重所有规则ID，useCount每个规则只减1次
        java.util.Set<Integer> allRuleIds = new java.util.LinkedHashSet<>();
        allRuleIds.addAll(complyIds);
        allRuleIds.addAll(violateIds);

        // 先统一回退 useCount（去重后每个规则只减1次）
        for (Integer ruleId : allRuleIds) {
            TransactionRule rule = transactionSystemTrialMapper.getTransactionRuleById(ruleId);
            if (rule != null) {
                rule.setUseCount(rule.getUseCount() != null && rule.getUseCount() > 0 ? rule.getUseCount() - 1 : 0);
                transactionSystemTrialMapper.updateTransactionRule(rule);
            }
        }

        // 回退遵守规则的成功次数
        for (Integer ruleId : complyIds) {
            if (isSuccess) {
                TransactionRule rule = transactionSystemTrialMapper.getTransactionRuleById(ruleId);
                if (rule != null) {
                    rule.setComplySuccessCount(rule.getComplySuccessCount() != null && rule.getComplySuccessCount() > 0 ? rule.getComplySuccessCount() - 1 : 0);
                    transactionSystemTrialMapper.updateTransactionRule(rule);
                }
            }
        }

        // 回退违反规则的违反次数和成功次数
        for (Integer ruleId : violateIds) {
            TransactionRule rule = transactionSystemTrialMapper.getTransactionRuleById(ruleId);
            if (rule != null) {
                rule.setViolateCount(rule.getViolateCount() != null && rule.getViolateCount() > 0 ? rule.getViolateCount() - 1 : 0);
                if (isSuccess) {
                    rule.setViolateSuccessCount(rule.getViolateSuccessCount() != null && rule.getViolateSuccessCount() > 0 ? rule.getViolateSuccessCount() - 1 : 0);
                }
                transactionSystemTrialMapper.updateTransactionRule(rule);
            }
        }
    }
}
