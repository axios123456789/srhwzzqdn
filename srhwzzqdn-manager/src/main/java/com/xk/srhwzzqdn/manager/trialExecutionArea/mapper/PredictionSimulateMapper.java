package com.xk.srhwzzqdn.manager.trialExecutionArea.mapper;

import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionReportDto;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionSimulateDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.PredictionSimulate;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.SimulateLedger;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.PredictionReportVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PredictionSimulateMapper {
    //====================预测模拟记录====================
    //条件查询预测模拟列表
    List<PredictionSimulate> getPredictionByCondition(PredictionSimulateDto dto);

    //添加预测模拟记录
    void addPrediction(PredictionSimulate predictionSimulate);

    //修改预测模拟记录
    void updatePrediction(PredictionSimulate predictionSimulate);

    //根据id删除预测模拟记录
    @Delete("delete from t_trial_prediction where id = #{param1}")
    void deletePredictionById(Long id);

    //根据ids批量删除预测模拟记录
    void deleteAllPredictionByIds(List<Long> ids);

    //根据id查询预测模拟记录
    @Select("select * from t_trial_prediction where id = #{param1}")
    PredictionSimulate getPredictionById(Long id);

    //====================模拟台账====================
    //查询模拟台账列表
    List<SimulateLedger> getSimulateLedgerByOwner(String owner);

    //添加模拟台账
    void addSimulateLedger(SimulateLedger simulateLedger);

    //修改模拟台账
    void updateSimulateLedger(SimulateLedger simulateLedger);

    //根据id删除模拟台账
    @Delete("delete from t_trial_simulate_ledger where id = #{param1}")
    void deleteSimulateLedgerById(Long id);

    //根据id查询模拟台账
    @Select("select * from t_trial_simulate_ledger where id = #{param1}")
    SimulateLedger getSimulateLedgerById(Long id);

    //根据owner和assetType查询台账
    @Select("select * from t_trial_simulate_ledger where owner = #{param1} and asset_type = #{param2} limit 1")
    SimulateLedger getLedgerByOwnerAndAssetType(String owner, Integer assetType);

    //根据owner和assetCode查询台账
    @Select("select * from t_trial_simulate_ledger where owner = #{param1} and asset_code = #{param2} limit 1")
    SimulateLedger getLedgerByOwnerAndAssetCode(String owner, String assetCode);

    //====================统计报表====================
    //统计总预测次数
    Integer countTotalPrediction(PredictionReportDto dto);

    //统计成功次数
    Integer countSuccessPrediction(PredictionReportDto dto);

    //按依据类型统计
    List<Map<String, Object>> statByBasisType(PredictionReportDto dto);

    //按预测情况统计
    List<Map<String, Object>> statBySituation(PredictionReportDto dto);

    //按月统计趋势
    List<Map<String, Object>> statByMonth(PredictionReportDto dto);

    //模拟操作统计
    List<Map<String, Object>> statSimulateTrade(PredictionReportDto dto);

    //按股票统计
    List<Map<String, Object>> statByStock(PredictionReportDto dto);

    //穿透明细查询：根据报表条件+穿透维度查询预测记录明细
    List<PredictionSimulate> getPredictionDetailByCondition(PredictionReportDto dto);
}
