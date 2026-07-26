package com.xk.srhwzzqdn.manager.trialExecutionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.PredictionSimulateService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionReportDto;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionSimulateDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.PredictionSimulate;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.SimulateLedger;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.PredictionReportVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/trialExecution/predictionSimulate")
public class PredictionSimulateController {
    @Autowired
    private PredictionSimulateService predictionSimulateService;

    /**
     * 条件分页查询预测模拟列表
     */
    @RequestMapping("/getPredictionByConditionAndPage/{current}/{limit}")
    public Result getPredictionByConditionAndPage(@PathVariable("current") Integer current,
                                                   @PathVariable("limit") Integer limit,
                                                   @RequestBody PredictionSimulateDto dto) {
        PageInfo<PredictionSimulate> pageInfo = predictionSimulateService.getPredictionByConditionAndPage(current, limit, dto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存预测模拟记录
     */
    @PostMapping("/savePrediction")
    public Result savePrediction(@RequestBody PredictionSimulate predictionSimulate) {
        try {
            predictionSimulateService.savePrediction(predictionSimulate);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存预测模拟记录失败！");
        }
    }

    /**
     * 根据id删除预测模拟记录
     */
    @DeleteMapping("/deletePredictionById/{id}")
    public Result deletePredictionById(@PathVariable("id") Long id) {
        try {
            predictionSimulateService.deletePredictionById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除预测模拟记录失败！");
        }
    }

    /**
     * 根据ids批量删除预测模拟记录
     */
    @PostMapping("/deleteAllPredictionByIds")
    public Result deleteAllPredictionByIds(@RequestBody List<Long> ids) {
        try {
            predictionSimulateService.deleteAllPredictionByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除预测模拟记录失败！");
        }
    }

    //====================模拟台账接口====================

    /**
     * 查询模拟台账列表
     */
    @RequestMapping("/getSimulateLedgerList")
    public Result getSimulateLedgerList() {
        List<SimulateLedger> list = predictionSimulateService.getSimulateLedgerList();
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存模拟台账
     */
    @PostMapping("/saveSimulateLedger")
    public Result saveSimulateLedger(@RequestBody SimulateLedger simulateLedger) {
        try {
            predictionSimulateService.saveSimulateLedger(simulateLedger);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存模拟台账失败！");
        }
    }

    /**
     * 根据id删除模拟台账
     */
    @DeleteMapping("/deleteSimulateLedgerById/{id}")
    public Result deleteSimulateLedgerById(@PathVariable("id") Long id) {
        try {
            predictionSimulateService.deleteSimulateLedgerById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除模拟台账失败！");
        }
    }

    //====================统计报表接口====================

    /**
     * 获取预测统计报表
     */
    @PostMapping("/getPredictionReport")
    public Result getPredictionReport(@RequestBody PredictionReportDto dto) {
        PredictionReportVo vo = predictionSimulateService.getPredictionReport(dto);
        return Result.build(vo, ResultCodeEnum.SUCCESS);
    }
}
