package com.xk.srhwzzqdn.manager.trialExecutionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionReportDto;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionSimulateDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.PredictionSimulate;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.SimulateLedger;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.PredictionReportVo;

import java.util.List;

public interface PredictionSimulateService {
    /**
     * 条件分页查询预测模拟列表
     */
    PageInfo<PredictionSimulate> getPredictionByConditionAndPage(Integer current, Integer limit, PredictionSimulateDto dto);

    /**
     * 保存预测模拟记录（含台账联动）
     */
    void savePrediction(PredictionSimulate predictionSimulate);

    /**
     * 根据id删除预测模拟记录（含台账回退）
     */
    void deletePredictionById(Long id);

    /**
     * 根据ids批量删除预测模拟记录（含台账回退）
     */
    void deleteAllPredictionByIds(List<Long> ids);

    /**
     * 查询模拟台账列表
     */
    List<SimulateLedger> getSimulateLedgerList();

    /**
     * 保存模拟台账
     */
    void saveSimulateLedger(SimulateLedger simulateLedger);

    /**
     * 根据id删除模拟台账
     */
    void deleteSimulateLedgerById(Long id);

    /**
     * 获取预测统计报表
     */
    PredictionReportVo getPredictionReport(PredictionReportDto dto);
}
