package com.xk.srhwzzqdn.manager.trialExecutionArea.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.mapper.PredictionSimulateMapper;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.PredictionSimulateService;
import com.xk.srhwzzqdn.manager.util.AiCommonUtil;
import com.xk.srhwzzqdn.manager.util.AiPromptUtil;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionReportDto;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionSimulateDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.PredictionSimulate;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.SimulateLedger;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.PredictionReportVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superBrain/trialExecution/predictionSimulate")
public class PredictionSimulateController {
    private static final Logger logger = LoggerFactory.getLogger(PredictionSimulateController.class);

    @Autowired
    private PredictionSimulateService predictionSimulateService;

    @Autowired
    private AiCommonUtil aiCommonUtil;

    @Autowired
    private PredictionSimulateMapper predictionSimulateMapper;

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

    /**
     * 删除当前用户所有模拟台账
     */
    @DeleteMapping("/deleteAllSimulateLedger")
    public Result deleteAllSimulateLedger() {
        try {
            predictionSimulateService.deleteAllSimulateLedgerByOwner();
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除所有台账失败！");
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

    /**
     * 穿透明细查询：根据报表条件+穿透维度分页查询预测记录明细
     */
    @RequestMapping("/getPredictionDetailByCondition/{current}/{limit}")
    public Result getPredictionDetailByCondition(@PathVariable("current") Integer current,
                                                  @PathVariable("limit") Integer limit,
                                                  @RequestBody PredictionReportDto dto) {
        PageInfo<PredictionSimulate> pageInfo = predictionSimulateService.getPredictionDetailByCondition(current, limit, dto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * AI智能预测：根据股票名称+代码，AI自动预测涨跌及第一页内容
     */
    @PostMapping("/aiPredict")
    public Result aiPredict(@RequestBody Map<String, Object> formData) {
        try {
            String stockName = AiPromptUtil.safeStr(formData.get("stockName"));
            String stockCode = AiPromptUtil.safeStr(formData.get("stockCode"));

            // 查询历史预测作为参考样例
            List<PredictionSimulate> recentList = predictionSimulateMapper.getRecentPredictions(5);
            StringBuilder historyRef = new StringBuilder();
            for (PredictionSimulate p : recentList) {
                historyRef.append(String.format("股票：%s(%s)，预测：%s，内容：%s，依据：%s\n",
                        p.getStockName(), p.getStockCode(),
                        p.getRiseFallPrediction() != null && p.getRiseFallPrediction() == 1 ? "涨" : "跌",
                        p.getPredictionContent(), p.getPredictionBasis()));
            }
            if (historyRef.length() == 0) {
                historyRef.append("暂无历史预测记录");
            }

            String prompt = AiPromptUtil.buildPredictionPrompt(new Object[]{
                    stockName, stockCode, historyRef.toString()
            });

            String aiResult = aiCommonUtil.callWithSystem(AiPromptUtil.PREDICTION_SYSTEM, prompt);
            if (aiResult == null || aiResult.trim().isEmpty()) {
                return Result.build(null, 500, "AI预测失败，请检查AI配置");
            }

            String jsonStr = extractJson(aiResult);
            JSONObject json = JSON.parseObject(jsonStr);

            JSONObject result = new JSONObject();
            result.put("riseFallPrediction", json.getInteger("riseFallPrediction"));
            result.put("basisType", json.getString("basisType"));
            result.put("predictionContent", json.getString("predictionContent"));
            result.put("predictionBasis", json.getString("predictionBasis"));
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("AI智能预测失败", e);
            return Result.build(null, 500, "AI预测失败：" + e.getMessage());
        }
    }

    /**
     * 从AI响应中提取JSON字符串（处理markdown代码块包裹的情况）
     */
    private String extractJson(String aiResult) {
        String trimmed = aiResult.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
