package com.xk.srhwzzqdn.manager.trialExecutionArea.mapper;

import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TransactionSystemTrialDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionRule;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionSystemTrial;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TransactionSystemTrialMapper {
    //条件查询交易系统试验列表
    List<TransactionSystemTrial> getTransactionSystemTrialByCondition(TransactionSystemTrialDto dto);

    //添加交易系统试验
    void addTransactionSystemTrial(TransactionSystemTrial transactionSystemTrial);

    //修改交易系统试验
    void updateTransactionSystemTrial(TransactionSystemTrial transactionSystemTrial);

    //根据id删除交易系统试验
    @Delete("delete from t_trial_transaction_record where id = #{param1}")
    void deleteTransactionSystemTrialById(Long id);

    //根据ids批量删除交易系统试验
    void deleteAllTransactionSystemTrialByIds(List<Long> ids);

    //根据id查询交易系统试验
    @Select("select * from t_trial_transaction_record where id = #{param1}")
    TransactionSystemTrial getTransactionSystemTrialById(Long id);

    //====================交易规则====================
    //条件查询交易规则列表
    List<TransactionRule> getTransactionRuleByCondition(String owner);

    //添加交易规则
    void addTransactionRule(TransactionRule transactionRule);

    //修改交易规则
    void updateTransactionRule(TransactionRule transactionRule);

    //根据id删除交易规则
    @Delete("delete from t_trial_transaction_rule where id = #{param1}")
    void deleteTransactionRuleById(Integer id);

    //根据ids批量删除交易规则
    void deleteAllTransactionRuleByIds(List<Integer> ids);

    //根据id查询交易规则
    @Select("select * from t_trial_transaction_rule where id = #{param1}")
    TransactionRule getTransactionRuleById(Integer id);
}