package com.xk.srhwzzqdn.manager.personalObservationArea.service;

import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerPageVo;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;
import com.xk.srhwzzqdn.model.vo.personalObservationArea.AssetTypeStatisticsVo;
import com.xk.srhwzzqdn.model.vo.personalObservationArea.SubTypeAnalysisVo;

import java.util.List;

public interface AssetDynamicsObservationService {

    // 报表1：资产类型分组统计
    List<AssetTypeStatisticsVo> getAssetTypeStatistics();

    // 报表2：基金子类分析
    List<SubTypeAnalysisVo> getFundSubTypeAnalysis();

    // 报表3：股票子类分析
    List<SubTypeAnalysisVo> getStockSubTypeAnalysis();

    // 穿透明细：按资产类型查询（分页）
    AssetLedgerPageVo getAssetDetailByType(Integer assetType, Integer page, Integer limit);

    // 穿透明细：按子类+清仓状态查询（分页）
    AssetLedgerPageVo getAssetDetailBySubTypeAndStatus(Integer assetType, Integer assetSubType, Boolean isCleared, Integer page, Integer limit);

    // 图表：资产类型金额扇形图
    List<GroupTextValueVo> getAssetTypeDistribution();

    // 图表：指定资产类型的子类金额扇形图
    List<GroupTextValueVo> getSubTypeDistribution(Integer assetType);

    // 图表：已清仓资产收益条形图
    List<GroupTextValueVo> getClearedAssetProfit();
}
