package com.xk.srhwzzqdn.manager.trialExecutionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TransactionSystemTrialDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionRule;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionSystemTrial;

import java.util.List;

public interface TransactionSystemTrialService {
    //条件分页查询交易系统试验列表
    PageInfo<TransactionSystemTrial> getTransactionSystemTrialByConditionAndPage(Integer current, Integer limit, TransactionSystemTrialDto dto);

    //保存交易系统试验
    void saveTransactionSystemTrial(TransactionSystemTrial transactionSystemTrial);

    //根据id删除交易系统试验
    void deleteTransactionSystemTrialById(Long id);

    //根据ids批量删除交易系统试验
    void deleteAllTransactionSystemTrialByIds(List<Long> ids);

    //====================交易规则====================
    //查询交易规则列表
    List<TransactionRule> getTransactionRuleList();

    //保存交易规则
    void saveTransactionRule(TransactionRule transactionRule);

    //根据id删除交易规则
    void deleteTransactionRuleById(Integer id);

    //根据ids批量删除交易规则
    void deleteAllTransactionRuleByIds(List<Integer> ids);
}