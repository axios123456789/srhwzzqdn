package com.xk.srhwzzqdn.manager.trialExecutionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.mapper.TransactionSystemTrialMapper;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.TransactionSystemTrialService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TransactionSystemTrialDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionSystemTrial;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionRule;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionSystemTrialServiceImpl implements TransactionSystemTrialService {
    @Autowired
    private TransactionSystemTrialMapper transactionSystemTrialMapper;

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
