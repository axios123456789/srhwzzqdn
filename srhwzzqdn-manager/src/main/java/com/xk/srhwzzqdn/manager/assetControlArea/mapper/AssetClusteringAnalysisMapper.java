package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.vo.assetControl.AssetStructureGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.InvestmentReturnAnalysisGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.TransactionAmountGroup;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AssetClusteringAnalysisMapper {
    //资产结构(总资产，储蓄资产，投资资产)数据获取
    AssetStructureGroupVo getAssetStructure(String asset_owner);

    //资产聚类分析-资产台账分析-资产类型分组数据获取
    List<GroupTextValueVo> getAssetTypeGroup(String asset_owner);

    //资产聚类分析-资产台账分析-资产状态分布分组数据获取
    List<GroupTextValueVo> getAssetStatusGroup(String asset_owner);

    //资产聚类分析-资产台账分析-资产金额排行数据获取
    List<GroupTextValueVo> getAssetAmountRank(String asset_owner);

    //资产聚类分析-资产台账分析-投资收益分析数据获取
    List<InvestmentReturnAnalysisGroupVo> getInvestmentReturnAnalysis(String asset_owner);

    //资产聚类分析-资产记账分析-收支金额分组数据获取
    List<TransactionAmountGroup> getTransactionAmountGroup(String begin_time, String end_time, String asset_owner);

    //资产聚类分析-资产记账分析-支出结构分组数据获取
    List<GroupTextValueVo> getExpenseStructureGroup(String begin_time, String end_time, String asset_owner);

    //资产聚类分析-资产记账分析-支出类型分组数据获取
    List<GroupTextValueVo> getSpendingTypeGroup(String begin_time, String end_time, String asset_owner);

    //资产聚类分析-资产记账分析-收入来源分组数据获取
    List<GroupTextValueVo> getIncomeSourceGroup(String begin_time, String end_time, String asset_owner);
}
