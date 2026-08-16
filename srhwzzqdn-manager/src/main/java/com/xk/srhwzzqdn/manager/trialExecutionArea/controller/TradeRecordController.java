package com.xk.srhwzzqdn.manager.trialExecutionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.TradeRecordService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.TradeRecordDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.TradeRecord;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/trialExecution/tradeRecord")
public class TradeRecordController {
    @Autowired
    private TradeRecordService tradeRecordService;

    /**
     * 条件分页查询交易记录列表
     */
    @PostMapping("/getTradeRecordByConditionAndPage/{current}/{limit}")
    public Result getTradeRecordByConditionAndPage(@PathVariable("current") Integer current,
                                                    @PathVariable("limit") Integer limit,
                                                    @RequestBody TradeRecordDto dto) {
        PageInfo<TradeRecord> pageInfo = tradeRecordService.getTradeRecordByConditionAndPage(current, limit, dto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存交易记录
     */
    @PostMapping("/saveTradeRecord")
    public Result saveTradeRecord(@RequestBody TradeRecord tradeRecord) {
        try {
            tradeRecordService.saveTradeRecord(tradeRecord);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存交易记录失败！");
        }
    }

    /**
     * 根据id删除交易记录
     */
    @DeleteMapping("/deleteTradeRecordById/{id}")
    public Result deleteTradeRecordById(@PathVariable("id") Long id) {
        try {
            tradeRecordService.deleteTradeRecordById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除交易记录失败！");
        }
    }

    /**
     * 根据ids批量删除交易记录
     */
    @PostMapping("/deleteAllTradeRecordByIds")
    public Result deleteAllTradeRecordByIds(@RequestBody List<Long> ids) {
        try {
            tradeRecordService.deleteAllTradeRecordByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除交易记录失败！");
        }
    }

    /**
     * 根据复盘日期汇总当天交易数据（交易次数、盈利次数、总盈亏%）
     */
    @GetMapping("/statTradeByReviewDate/{reviewDate}")
    public Result statTradeByReviewDate(@PathVariable("reviewDate") String reviewDate) {
        try {
            return Result.build(tradeRecordService.statTradeByReviewDate(reviewDate), ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "汇总交易数据失败！");
        }
    }
}