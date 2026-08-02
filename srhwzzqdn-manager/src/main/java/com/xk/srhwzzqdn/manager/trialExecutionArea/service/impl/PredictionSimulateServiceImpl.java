package com.xk.srhwzzqdn.manager.trialExecutionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.mapper.PredictionSimulateMapper;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.PredictionSimulateService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionReportDto;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.PredictionSimulateDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.PredictionSimulate;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.SimulateLedger;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.PredictionReportVo;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PredictionSimulateServiceImpl implements PredictionSimulateService {
    @Autowired
    private PredictionSimulateMapper predictionSimulateMapper;

    /**
     * 条件分页查询预测模拟列表
     */
    @Override
    public PageInfo<PredictionSimulate> getPredictionByConditionAndPage(Integer current, Integer limit, PredictionSimulateDto dto) {
        PageHelper.startPage(current, limit);
        dto.setOwner(AuthContextUtil.get().getId());
        List<PredictionSimulate> list = predictionSimulateMapper.getPredictionByCondition(dto);
        return new PageInfo<>(list);
    }

    /**
     * 保存预测模拟记录（含台账联动）
     */
    @Override
    public void savePrediction(PredictionSimulate predictionSimulate) {
        String owner = AuthContextUtil.get().getId();
        if (predictionSimulate.getId() == null) {
            // 新增
            predictionSimulate.setOwner(owner);
            predictionSimulateMapper.addPrediction(predictionSimulate);
            // 台账联动
            processLedgerOnSave(predictionSimulate);
        } else {
            // 修改：先查旧记录回退台账，再保存新记录，再联动新台账
            PredictionSimulate oldRecord = predictionSimulateMapper.getPredictionById(predictionSimulate.getId());
            predictionSimulateMapper.updatePrediction(predictionSimulate);
            // 回退旧台账
            if (oldRecord != null) {
                rollbackLedger(oldRecord);
            }
            // 联动新台账
            PredictionSimulate newRecord = predictionSimulateMapper.getPredictionById(predictionSimulate.getId());
            if (newRecord != null) {
                processLedgerOnSave(newRecord);
            }
        }
    }

    /**
     * 根据id删除预测模拟记录（含台账回退）
     */
    @Override
    public void deletePredictionById(Long id) {
        PredictionSimulate record = predictionSimulateMapper.getPredictionById(id);
        predictionSimulateMapper.deletePredictionById(id);
        if (record != null) {
            rollbackLedger(record);
        }
    }

    /**
     * 根据ids批量删除预测模拟记录（含台账回退）
     */
    @Override
    public void deleteAllPredictionByIds(List<Long> ids) {
        for (Long id : ids) {
            PredictionSimulate record = predictionSimulateMapper.getPredictionById(id);
            if (record != null) {
                rollbackLedger(record);
            }
        }
        predictionSimulateMapper.deleteAllPredictionByIds(ids);
    }

    /**
     * 查询模拟台账列表
     */
    @Override
    public List<SimulateLedger> getSimulateLedgerList() {
        String owner = AuthContextUtil.get().getId();
        return predictionSimulateMapper.getSimulateLedgerByOwner(owner);
    }

    /**
     * 保存模拟台账
     */
    @Override
    public void saveSimulateLedger(SimulateLedger simulateLedger) {
        if (simulateLedger.getId() == null) {
            simulateLedger.setOwner(AuthContextUtil.get().getId());
            predictionSimulateMapper.addSimulateLedger(simulateLedger);
        } else {
            predictionSimulateMapper.updateSimulateLedger(simulateLedger);
        }
    }

    /**
     * 根据id删除模拟台账
     */
    @Override
    public void deleteSimulateLedgerById(Long id) {
        predictionSimulateMapper.deleteSimulateLedgerById(id);
    }

    /**
     * 删除当前用户所有模拟台账
     */
    @Override
    public void deleteAllSimulateLedgerByOwner() {
        String owner = AuthContextUtil.get().getId();
        predictionSimulateMapper.deleteAllSimulateLedgerByOwner(owner);
    }

    /**
     * 获取预测统计报表
     */
    @Override
    public PredictionReportVo getPredictionReport(PredictionReportDto dto) {
        dto.setOwner(AuthContextUtil.get().getId());
        PredictionReportVo vo = new PredictionReportVo();

        // 总体统计
        Integer totalCount = predictionSimulateMapper.countTotalPrediction(dto);
        Integer successCount = predictionSimulateMapper.countSuccessPrediction(dto);
        totalCount = totalCount != null ? totalCount : 0;
        successCount = successCount != null ? successCount : 0;
        vo.setTotalCount(totalCount);
        vo.setSuccessCount(successCount);
        vo.setFailCount(totalCount - successCount);
        if (totalCount > 0) {
            vo.setAccuracyRate(new BigDecimal(successCount).divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
        } else {
            vo.setAccuracyRate(BigDecimal.ZERO);
        }

        // 按依据类型统计
        List<Map<String, Object>> basisTypeRaw = predictionSimulateMapper.statByBasisType(dto);
        List<PredictionReportVo.BasisTypeStat> basisTypeStats = new ArrayList<>();
        for (Map<String, Object> map : basisTypeRaw) {
            PredictionReportVo.BasisTypeStat stat = new PredictionReportVo.BasisTypeStat();
            stat.setBasisType(String.valueOf(map.get("basisType")));
            stat.setCount(((Number) map.get("count")).intValue());
            stat.setSuccessCount(((Number) map.get("successCount")).intValue());
            if (stat.getCount() > 0) {
                stat.setSuccessRate(new BigDecimal(stat.getSuccessCount()).divide(new BigDecimal(stat.getCount()), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
            } else {
                stat.setSuccessRate(BigDecimal.ZERO);
            }
            basisTypeStats.add(stat);
        }
        vo.setBasisTypeStats(basisTypeStats);

        // 按预测情况统计
        List<Map<String, Object>> situationRaw = predictionSimulateMapper.statBySituation(dto);
        List<PredictionReportVo.SituationStat> situationStats = new ArrayList<>();
        int situationTotal = 0;
        for (Map<String, Object> map : situationRaw) {
            situationTotal += ((Number) map.get("count")).intValue();
        }
        for (Map<String, Object> map : situationRaw) {
            PredictionReportVo.SituationStat stat = new PredictionReportVo.SituationStat();
            stat.setPredictionSituation(((Number) map.get("predictionSituation")).intValue());
            stat.setCount(((Number) map.get("count")).intValue());
            if (situationTotal > 0) {
                stat.setPercentage(new BigDecimal(stat.getCount()).divide(new BigDecimal(situationTotal), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
            } else {
                stat.setPercentage(BigDecimal.ZERO);
            }
            situationStats.add(stat);
        }
        vo.setSituationStats(situationStats);

        // 按月统计趋势
        List<Map<String, Object>> monthRaw = predictionSimulateMapper.statByMonth(dto);
        List<PredictionReportVo.MonthlyTrend> monthlyTrends = new ArrayList<>();
        for (Map<String, Object> map : monthRaw) {
            PredictionReportVo.MonthlyTrend trend = new PredictionReportVo.MonthlyTrend();
            trend.setMonth((String) map.get("month"));
            trend.setTotalCount(((Number) map.get("totalCount")).intValue());
            trend.setSuccessCount(((Number) map.get("successCount")).intValue());
            if (trend.getTotalCount() > 0) {
                trend.setSuccessRate(new BigDecimal(trend.getSuccessCount()).divide(new BigDecimal(trend.getTotalCount()), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
            } else {
                trend.setSuccessRate(BigDecimal.ZERO);
            }
            monthlyTrends.add(trend);
        }
        vo.setMonthlyTrends(monthlyTrends);

        // 模拟操作统计
        List<Map<String, Object>> tradeRaw = predictionSimulateMapper.statSimulateTrade(dto);
        PredictionReportVo.SimulateTradeStat tradeStat = new PredictionReportVo.SimulateTradeStat();
        if (tradeRaw != null && !tradeRaw.isEmpty()) {
            Map<String, Object> tradeMap = tradeRaw.get(0);
            tradeStat.setTotalBuyCount(tradeMap.get("totalBuyCount") != null ? ((Number) tradeMap.get("totalBuyCount")).intValue() : 0);
            tradeStat.setTotalSellCount(tradeMap.get("totalSellCount") != null ? ((Number) tradeMap.get("totalSellCount")).intValue() : 0);
            tradeStat.setTotalBuyAmount(tradeMap.get("totalBuyAmount") != null ? new BigDecimal(tradeMap.get("totalBuyAmount").toString()) : BigDecimal.ZERO);
            tradeStat.setTotalSellAmount(tradeMap.get("totalSellAmount") != null ? new BigDecimal(tradeMap.get("totalSellAmount").toString()) : BigDecimal.ZERO);
            tradeStat.setTotalHandlingFee(tradeMap.get("totalHandlingFee") != null ? new BigDecimal(tradeMap.get("totalHandlingFee").toString()) : BigDecimal.ZERO);
            tradeStat.setTotalProfitLoss(tradeStat.getTotalSellAmount().subtract(tradeStat.getTotalBuyAmount()).subtract(tradeStat.getTotalHandlingFee()));
        } else {
            tradeStat.setTotalBuyCount(0);
            tradeStat.setTotalSellCount(0);
            tradeStat.setTotalBuyAmount(BigDecimal.ZERO);
            tradeStat.setTotalSellAmount(BigDecimal.ZERO);
            tradeStat.setTotalHandlingFee(BigDecimal.ZERO);
            tradeStat.setTotalProfitLoss(BigDecimal.ZERO);
        }
        vo.setSimulateTradeStat(tradeStat);

        // 按股票统计
        List<Map<String, Object>> stockRaw = predictionSimulateMapper.statByStock(dto);
        List<PredictionReportVo.StockStat> stockStats = new ArrayList<>();
        for (Map<String, Object> map : stockRaw) {
            PredictionReportVo.StockStat stat = new PredictionReportVo.StockStat();
            stat.setStockName((String) map.get("stockName"));
            stat.setStockCode((String) map.get("stockCode"));
            stat.setPredictCount(((Number) map.get("predictCount")).intValue());
            stat.setSuccessCount(((Number) map.get("successCount")).intValue());
            if (stat.getPredictCount() > 0) {
                stat.setSuccessRate(new BigDecimal(stat.getSuccessCount()).divide(new BigDecimal(stat.getPredictCount()), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
            } else {
                stat.setSuccessRate(BigDecimal.ZERO);
            }
            stockStats.add(stat);
        }
        vo.setStockStats(stockStats);

        return vo;
    }

    /**
     * 穿透明细查询：根据报表条件+穿透维度分页查询预测记录明细
     */
    @Override
    public PageInfo<PredictionSimulate> getPredictionDetailByCondition(Integer current, Integer limit, PredictionReportDto dto) {
        dto.setOwner(AuthContextUtil.get().getId());
        PageHelper.startPage(current, limit);
        List<PredictionSimulate> list = predictionSimulateMapper.getPredictionDetailByCondition(dto);
        return new PageInfo<>(list);
    }

    //====================台账联动辅助方法====================

    /**
     * 保存预测记录时联动台账（仿照证券账户买入卖出）
     */
    private void processLedgerOnSave(PredictionSimulate record) {
        if (record.getSimulateOperation() == null || record.getTradeShare() == null || record.getCurrentPrice() == null) {
            return;
        }
        String owner = record.getOwner();
        BigDecimal tradeAmount = new BigDecimal(record.getTradeShare()).multiply(record.getCurrentPrice());
        BigDecimal fee = record.getHandlingFee() != null ? record.getHandlingFee() : BigDecimal.ZERO;

        if (record.getSimulateOperation() == 1) {
            // 模拟买入
            // 1. 扣减总账户金额（可用资金减少）
            SimulateLedger mainAccount = predictionSimulateMapper.getLedgerByOwnerAndAssetType(owner, 1);
            if (mainAccount != null) {
                mainAccount.setAssetAmount(mainAccount.getAssetAmount().subtract(tradeAmount).subtract(fee));
                predictionSimulateMapper.updateSimulateLedger(mainAccount);
            }
            // 2. 增加股票台账：若已存在则修改（数量+金额累加），不存在则新增
            SimulateLedger stockLedger = predictionSimulateMapper.getLedgerByOwnerAndAssetCode(owner, record.getStockCode());
            if (stockLedger != null) {
                // 台账已存在（可能之前全部卖出后数量为0），修改而非新增
                stockLedger.setAssetQuantity(stockLedger.getAssetQuantity() + record.getTradeShare());
                stockLedger.setAssetAmount(stockLedger.getAssetAmount().add(tradeAmount));
                stockLedger.setAssetName(record.getStockName()); // 更新名称
                predictionSimulateMapper.updateSimulateLedger(stockLedger);
            } else {
                // 新增股票台账记录
                SimulateLedger newLedger = new SimulateLedger();
                newLedger.setAssetName(record.getStockName());
                newLedger.setAssetCode(record.getStockCode());
                newLedger.setAssetType(2); // 默认A股
                newLedger.setAssetAmount(tradeAmount);
                newLedger.setAssetQuantity(record.getTradeShare());
                newLedger.setOwner(owner);
                predictionSimulateMapper.addSimulateLedger(newLedger);
            }
        } else if (record.getSimulateOperation() == 2) {
            // 模拟卖出
            // 1. 减少股票台账：数量和金额减少，全部卖出后设为0而非删除
            SimulateLedger stockLedger = predictionSimulateMapper.getLedgerByOwnerAndAssetCode(owner, record.getStockCode());
            if (stockLedger != null) {
                stockLedger.setAssetQuantity(stockLedger.getAssetQuantity() - record.getTradeShare());
                stockLedger.setAssetAmount(stockLedger.getAssetAmount().subtract(tradeAmount));
                // 全部卖出后数量和金额设为0，保留台账记录
                if (stockLedger.getAssetQuantity() <= 0) {
                    stockLedger.setAssetQuantity(0);
                    stockLedger.setAssetAmount(BigDecimal.ZERO);
                }
                predictionSimulateMapper.updateSimulateLedger(stockLedger);
            }
            // 2. 增加总账户金额（可用资金增加）
            SimulateLedger mainAccount = predictionSimulateMapper.getLedgerByOwnerAndAssetType(owner, 1);
            if (mainAccount != null) {
                mainAccount.setAssetAmount(mainAccount.getAssetAmount().add(tradeAmount).subtract(fee));
                predictionSimulateMapper.updateSimulateLedger(mainAccount);
            }
        }
    }

    /**
     * 回退台账（删除或修改时回退旧记录，仿照证券账户反向操作）
     */
    private void rollbackLedger(PredictionSimulate record) {
        if (record.getSimulateOperation() == null || record.getTradeShare() == null || record.getCurrentPrice() == null) {
            return;
        }
        String owner = record.getOwner();
        BigDecimal tradeAmount = new BigDecimal(record.getTradeShare()).multiply(record.getCurrentPrice());
        BigDecimal fee = record.getHandlingFee() != null ? record.getHandlingFee() : BigDecimal.ZERO;

        if (record.getSimulateOperation() == 1) {
            // 回退买入：总账户加回，股票台账减回
            SimulateLedger mainAccount = predictionSimulateMapper.getLedgerByOwnerAndAssetType(owner, 1);
            if (mainAccount != null) {
                mainAccount.setAssetAmount(mainAccount.getAssetAmount().add(tradeAmount).add(fee));
                predictionSimulateMapper.updateSimulateLedger(mainAccount);
            }
            SimulateLedger stockLedger = predictionSimulateMapper.getLedgerByOwnerAndAssetCode(owner, record.getStockCode());
            if (stockLedger != null) {
                stockLedger.setAssetQuantity(stockLedger.getAssetQuantity() - record.getTradeShare());
                stockLedger.setAssetAmount(stockLedger.getAssetAmount().subtract(tradeAmount));
                // 回退后数量为0则设为0，不删除
                if (stockLedger.getAssetQuantity() <= 0) {
                    stockLedger.setAssetQuantity(0);
                    stockLedger.setAssetAmount(BigDecimal.ZERO);
                }
                predictionSimulateMapper.updateSimulateLedger(stockLedger);
            }
        } else if (record.getSimulateOperation() == 2) {
            // 回退卖出：股票台账加回，总账户减回
            SimulateLedger stockLedger = predictionSimulateMapper.getLedgerByOwnerAndAssetCode(owner, record.getStockCode());
            if (stockLedger != null) {
                // 台账已存在，修改
                stockLedger.setAssetQuantity(stockLedger.getAssetQuantity() + record.getTradeShare());
                stockLedger.setAssetAmount(stockLedger.getAssetAmount().add(tradeAmount));
                predictionSimulateMapper.updateSimulateLedger(stockLedger);
            } else {
                // 台账不存在（可能之前被删除了），新增
                SimulateLedger newLedger = new SimulateLedger();
                newLedger.setAssetName(record.getStockName());
                newLedger.setAssetCode(record.getStockCode());
                newLedger.setAssetType(2);
                newLedger.setAssetAmount(tradeAmount);
                newLedger.setAssetQuantity(record.getTradeShare());
                newLedger.setOwner(owner);
                predictionSimulateMapper.addSimulateLedger(newLedger);
            }
            SimulateLedger mainAccount = predictionSimulateMapper.getLedgerByOwnerAndAssetType(owner, 1);
            if (mainAccount != null) {
                mainAccount.setAssetAmount(mainAccount.getAssetAmount().subtract(tradeAmount).add(fee));
                predictionSimulateMapper.updateSimulateLedger(mainAccount);
            }
        }
    }
}
