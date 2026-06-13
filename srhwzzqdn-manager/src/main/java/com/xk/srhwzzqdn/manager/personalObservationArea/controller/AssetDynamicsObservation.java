package com.xk.srhwzzqdn.manager.personalObservationArea.controller;

import com.xk.srhwzzqdn.manager.personalObservationArea.service.AssetDynamicsObservationService;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerPageVo;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.model.vo.personalObservationArea.AssetTypeStatisticsVo;
import com.xk.srhwzzqdn.model.vo.personalObservationArea.SubTypeAnalysisVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产动态观测
 */
@RestController
@RequestMapping("/superBrain/personalObservation/assetDynamicsObservation")
public class AssetDynamicsObservation {
    @Autowired
    private AssetDynamicsObservationService assetDynamicsObservationService;

    private static final Logger logger = LoggerFactory.getLogger(AssetDynamicsObservation.class);

    // ==================== 报表接口 ====================

    /**
     * 报表1：资产类型分组统计
     */
    @GetMapping("/getAssetTypeStatistics")
    public Result getAssetTypeStatistics() {
        try {
            List<AssetTypeStatisticsVo> list = assetDynamicsObservationService.getAssetTypeStatistics();
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取资产类型分组统计数据失败", e);
            return Result.build(null, 500, "获取资产类型分组统计数据失败！");
        }
    }

    /**
     * 报表2：基金子类分析
     */
    @GetMapping("/getFundSubTypeAnalysis")
    public Result getFundSubTypeAnalysis() {
        try {
            List<SubTypeAnalysisVo> list = assetDynamicsObservationService.getFundSubTypeAnalysis();
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取基金子类分析数据失败", e);
            return Result.build(null, 500, "获取基金子类分析数据失败！");
        }
    }

    /**
     * 报表3：股票子类分析
     */
    @GetMapping("/getStockSubTypeAnalysis")
    public Result getStockSubTypeAnalysis() {
        try {
            List<SubTypeAnalysisVo> list = assetDynamicsObservationService.getStockSubTypeAnalysis();
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取股票子类分析数据失败", e);
            return Result.build(null, 500, "获取股票子类分析数据失败！");
        }
    }

    // ==================== 穿透明细接口 ====================

    /**
     * 穿透明细：按资产类型查询（分页）
     */
    @GetMapping("/getAssetDetailByType")
    public Result getAssetDetailByType(@RequestParam Integer assetType,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer limit) {
        try {
            AssetLedgerPageVo pageVo = assetDynamicsObservationService.getAssetDetailByType(assetType, page, limit);
            return Result.build(pageVo, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取资产类型穿透明细数据失败", e);
            return Result.build(null, 500, "获取资产类型穿透明细数据失败！");
        }
    }

    /**
     * 穿透明细：按子类+清仓状态查询（分页）
     */
    @GetMapping("/getAssetDetailBySubTypeAndStatus")
    public Result getAssetDetailBySubTypeAndStatus(@RequestParam Integer assetType,
                                                    @RequestParam Integer assetSubType,
                                                    @RequestParam(required = false) Boolean isCleared,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer limit) {
        try {
            AssetLedgerPageVo pageVo = assetDynamicsObservationService.getAssetDetailBySubTypeAndStatus(assetType, assetSubType, isCleared, page, limit);
            return Result.build(pageVo, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取子类穿透明细数据失败", e);
            return Result.build(null, 500, "获取子类穿透明细数据失败！");
        }
    }

    // ==================== 图表接口 ====================

    /**
     * 图表：资产类型金额扇形图
     */
    @GetMapping("/getAssetTypeDistribution")
    public Result getAssetTypeDistribution() {
        try {
            List<GroupTextValueVo> list = assetDynamicsObservationService.getAssetTypeDistribution();
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取资产类型分布数据失败", e);
            return Result.build(null, 500, "获取资产类型分布数据失败！");
        }
    }

    /**
     * 图表：指定资产类型的子类金额扇形图
     */
    @GetMapping("/getSubTypeDistribution")
    public Result getSubTypeDistribution(@RequestParam Integer assetType) {
        try {
            List<GroupTextValueVo> list = assetDynamicsObservationService.getSubTypeDistribution(assetType);
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取子类分布数据失败", e);
            return Result.build(null, 500, "获取子类分布数据失败！");
        }
    }

    /**
     * 图表：已清仓资产收益条形图
     */
    @GetMapping("/getClearedAssetProfit")
    public Result getClearedAssetProfit() {
        try {
            List<GroupTextValueVo> list = assetDynamicsObservationService.getClearedAssetProfit();
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取已清仓资产收益数据失败", e);
            return Result.build(null, 500, "获取已清仓资产收益数据失败！");
        }
    }
}
