package com.xk.srhwzzqdn.manager.trialExecutionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.TransactionSystemTrialService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TransactionSystemTrialDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionSystemTrial;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TransactionRule;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/trialExecution/transactionSystemTrial")
public class TransactionSystemTrialController {
    @Autowired
    private TransactionSystemTrialService transactionSystemTrialService;

    /**
     * 条件分页查询交易系统试验列表
     * @param current
     * @param limit
     * @param dto
     * @return
     */
    @RequestMapping("/getTransactionSystemTrialByConditionAndPage/{current}/{limit}")
    public Result getTransactionSystemTrialByConditionAndPage(@PathVariable("current") Integer current,
                                                               @PathVariable("limit") Integer limit,
                                                               @RequestBody TransactionSystemTrialDto dto){
        PageInfo<TransactionSystemTrial> pageInfo = transactionSystemTrialService.getTransactionSystemTrialByConditionAndPage(current, limit, dto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存交易系统试验
     * @param transactionSystemTrial
     * @return
     */
    @PostMapping("/saveTransactionSystemTrial")
    public Result saveTransactionSystemTrial(@RequestBody TransactionSystemTrial transactionSystemTrial){
        try {
            transactionSystemTrialService.saveTransactionSystemTrial(transactionSystemTrial);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存交易系统试验失败！");
        }
    }

    /**
     * 根据id删除交易系统试验
     * @param id
     * @return
     */
    @DeleteMapping("/deleteTransactionSystemTrialById/{id}")
    public Result deleteTransactionSystemTrialById(@PathVariable("id") Long id){
        try {
            transactionSystemTrialService.deleteTransactionSystemTrialById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除交易系统试验失败！");
        }
    }

    /**
     * 根据ids批量删除交易系统试验
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllTransactionSystemTrialByIds")
    public Result deleteAllTransactionSystemTrialByIds(@RequestBody List<Long> ids){
        try {
            transactionSystemTrialService.deleteAllTransactionSystemTrialByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除交易系统试验失败！");
        }
    }

    //====================交易规则接口====================
    /**
     * 查询交易规则列表
     * @return
     */
    @RequestMapping("/getTransactionRuleList")
    public Result getTransactionRuleList(){
        List<TransactionRule> list = transactionSystemTrialService.getTransactionRuleList();
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存交易规则
     * @param transactionRule
     * @return
     */
    @PostMapping("/saveTransactionRule")
    public Result saveTransactionRule(@RequestBody TransactionRule transactionRule){
        try {
            transactionSystemTrialService.saveTransactionRule(transactionRule);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存交易规则失败！");
        }
    }

    /**
     * 根据id删除交易规则
     * @param id
     * @return
     */
    @DeleteMapping("/deleteTransactionRuleById/{id}")
    public Result deleteTransactionRuleById(@PathVariable("id") Integer id){
        try {
            transactionSystemTrialService.deleteTransactionRuleById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除交易规则失败！");
        }
    }

    /**
     * 根据ids批量删除交易规则
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllTransactionRuleByIds")
    public Result deleteAllTransactionRuleByIds(@RequestBody List<Integer> ids){
        try {
            transactionSystemTrialService.deleteAllTransactionRuleByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除交易规则失败！");
        }
    }
}