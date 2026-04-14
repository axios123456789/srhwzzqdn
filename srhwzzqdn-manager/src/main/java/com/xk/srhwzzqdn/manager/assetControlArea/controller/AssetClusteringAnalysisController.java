package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetClusteringAnalysisService;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetStructureGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.InvestmentReturnAnalysisGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.TransactionAmountGroup;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 资产聚类分析
 */
@RestController
@RequestMapping("/superBrain/assetControl/assetClusteringAnalysis")
public class AssetClusteringAnalysisController {
    @Autowired
    private AssetClusteringAnalysisService assetClusteringAnalysisService;

    private static final Logger logger = LoggerFactory.getLogger(AssetClusteringAnalysisController.class);

    /**
     * 资产聚类分析-资产台账分析-资产结构(总资产，储蓄资产，投资资产)数据获取
     * @return
     */
    @GetMapping("/getAssetStructure")
    public Result getAssetStructure(){
        AssetStructureGroupVo assetStructureGroupVo = assetClusteringAnalysisService.getAssetStructure();

        return Result.build(assetStructureGroupVo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 资产聚类分析-资产台账分析-资产类型分组数据获取
     * @return
     */
    @GetMapping("/getAssetTypeGroup")
    public Result getAssetTypeGroup(){
        try {
            List<GroupTextValueVo> groupTextValueVos = assetClusteringAnalysisService.getAssetTypeGroup();
            return Result.build(groupTextValueVos, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            //打印报错日志到控制台
            logger.error("获取资产类型分组数据失败", e);
            return Result.build(null, 500, "获取资产类型分组数据失败！");
        }
    }

    /**
     * 资产聚类分析-资产台账分析-资产状态分布分组数据获取
     * @return
     */
    @GetMapping("/getAssetStatusGroup")
    public Result getAssetStatusGroup(){
        try {
            List<GroupTextValueVo> groupTextValueVos = assetClusteringAnalysisService.getAssetStatusGroup();
            return Result.build(groupTextValueVos, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            //打印报错日志到控制台
            logger.error("获取资产状态分组数据失败", e);
            return Result.build(null, 500, "获取资产状态分组数据失败！");
        }
    }

    /**
     * 资产聚类分析-资产台账分析-资产金额排行数据获取
     * @return
     */
    @GetMapping("/getAssetAmountRank")
    public Result getAssetAmountRank(){
        try {
            List<GroupTextValueVo> groupTextValueVos = assetClusteringAnalysisService.getAssetAmountRank();
            return Result.build(groupTextValueVos, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取资产金额排名数据失败", e);
            return Result.build(null, 500, "获取资产金额排名数据失败！");
        }
    }

    /**
     * 资产聚类分析-资产台账分析-投资收益分析数据获取
     * @return
     */
    @GetMapping("/getInvestmentReturnAnalysis")
    public Result getInvestmentReturnAnalysis(){
        try {
            List<InvestmentReturnAnalysisGroupVo> investmentReturnAnalysisGroupVo = assetClusteringAnalysisService.getInvestmentReturnAnalysis();
            return Result.build(investmentReturnAnalysisGroupVo, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取资产金额排名数据失败", e);
            return Result.build(null, 500, "获取资产金额排名数据失败！");
        }
    }

    /**
     * 资产聚类分析-资产记账分析-收支金额分组数据获取
     * @param begin_time
     * @param end_time
     * @return
     */
    @GetMapping("/getTransactionAmountGroup/{begin_time}/{end_time}")
    public Result getTransactionAmountGroup(@PathVariable("begin_time") String begin_time,
                                            @PathVariable("end_time") String end_time){
        try {
            List<TransactionAmountGroup> transactionAmountGroup = assetClusteringAnalysisService.getTransactionAmountGroup(begin_time, end_time);
            return Result.build(transactionAmountGroup, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取收支金额数据失败", e);
            return Result.build(null, 500, "获取收支金额数据失败！");
        }
    }

    /**
     * 资产聚类分析-资产记账分析-支出结构分组数据获取
     * @param begin_time
     * @param end_time
     * @return
     */
    @GetMapping("/expenseStructureGroup/{begin_time}/{end_time}")
    public Result expenseStructureGroup(@PathVariable("begin_time") String begin_time,
                                        @PathVariable("end_time") String end_time){
        try {
            List<GroupTextValueVo> groupTextValueVos = assetClusteringAnalysisService.getExpenseStructureGroup(begin_time, end_time);
            return Result.build(groupTextValueVos, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取支出结构数据失败", e);
            return Result.build(null, 500, "获取支出结构数据失败！");
        }
    }

    /**
     * 资产聚类分析-资产记账分析-支出类型分组数据获取
     * @param begin_time
     * @param end_time
     * @return
     */
    @GetMapping("/getSpendingTypeGroup/{begin_time}/{end_time}")
    public Result getSpendingTypeGroup(@PathVariable("begin_time") String begin_time,
                                       @PathVariable("end_time") String end_time){
        try {
            List<GroupTextValueVo> groupTextValueVos = assetClusteringAnalysisService.getSpendingTypeGroup(begin_time, end_time);
            return Result.build(groupTextValueVos, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取支出类型数据失败", e);
            return Result.build(null, 500, "获取支出类型数据失败！");
        }
    }

    /**
     * 资产聚类分析-资产记账分析-收入来源分组数据获取
     * @param begin_time
     * @param end_time
     * @return
     */
    @GetMapping("/getIncomeSourceGroup/{begin_time}/{end_time}")
    public Result getIncomeSourceGroup(@PathVariable("begin_time") String begin_time,
                                       @PathVariable("end_time") String end_time){
        try {
            List<GroupTextValueVo> groupTextValueVos = assetClusteringAnalysisService.getIncomeSourceGroup(begin_time, end_time);
            return Result.build(groupTextValueVos, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取收入来源数据失败", e);
            return Result.build(null, 500, "获取收入来源数据失败！");
        }
    }
}
