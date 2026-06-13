package com.xk.srhwzzqdn.manager.personalObservationArea.mapper;

import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerVo;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;
import com.xk.srhwzzqdn.model.vo.personalObservationArea.AssetTypeStatisticsVo;
import com.xk.srhwzzqdn.model.vo.personalObservationArea.SubTypeAnalysisVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssetDynamicsObservationMapper {

    // 报表1：资产类型分组统计（储蓄/保险/基金/股票）
    List<AssetTypeStatisticsVo> getAssetTypeStatistics(@Param("assetOwner") String assetOwner);

    // 报表2：基金子类分析（asset_type=3）
    List<SubTypeAnalysisVo> getFundSubTypeAnalysis(@Param("assetOwner") String assetOwner);

    // 报表3：股票子类分析（asset_type=4）
    List<SubTypeAnalysisVo> getStockSubTypeAnalysis(@Param("assetOwner") String assetOwner);

    // 穿透明细：按资产类型查询资产列表
    List<AssetLedgerVo> getAssetDetailByType(@Param("assetOwner") String assetOwner,
                                              @Param("assetType") Integer assetType);

    // 穿透明细：按资产类型+子类+清仓状态查询资产列表
    List<AssetLedgerVo> getAssetDetailBySubTypeAndStatus(@Param("assetOwner") String assetOwner,
                                                          @Param("assetType") Integer assetType,
                                                          @Param("assetSubType") Integer assetSubType,
                                                          @Param("isCleared") Boolean isCleared);

    // 图表：资产类型金额扇形图
    List<GroupTextValueVo> getAssetTypeDistribution(@Param("assetOwner") String assetOwner);

    // 图表：指定资产类型的子类金额扇形图
    List<GroupTextValueVo> getSubTypeDistribution(@Param("assetOwner") String assetOwner,
                                                    @Param("assetType") Integer assetType);

    // 图表：已清仓资产收益条形图
    List<GroupTextValueVo> getClearedAssetProfit(@Param("assetOwner") String assetOwner);
}
