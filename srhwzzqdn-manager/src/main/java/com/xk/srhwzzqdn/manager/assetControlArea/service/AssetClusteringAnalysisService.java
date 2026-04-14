package com.xk.srhwzzqdn.manager.assetControlArea.service;

import com.xk.srhwzzqdn.model.vo.assetControl.AssetStructureGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.InvestmentReturnAnalysisGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.TransactionAmountGroup;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;

import java.util.List;

public interface AssetClusteringAnalysisService {
    //资产聚类分析-资产台账分析-资产结构(总资产，储蓄资产，投资资产)数据获取
    AssetStructureGroupVo getAssetStructure();

    //资产聚类分析-资产台账分析-资产类型分组数据获取
    List<GroupTextValueVo> getAssetTypeGroup();

    //资产聚类分析-资产台账分析-资产状态分布分组数据获取
    List<GroupTextValueVo> getAssetStatusGroup();

    //资产聚类分析-资产台账分析-资产金额排行数据获取
    List<GroupTextValueVo> getAssetAmountRank();

    //资产聚类分析-资产台账分析-投资收益分析数据获取
    List<InvestmentReturnAnalysisGroupVo> getInvestmentReturnAnalysis();

    //资产聚类分析-资产记账分析-收支金额分组数据获取
    List<TransactionAmountGroup> getTransactionAmountGroup(String begin_time, String end_time);

    //资产聚类分析-资产记账分析-支出结构分组数据获取
    List<GroupTextValueVo> getExpenseStructureGroup(String begin_time, String end_time);

    //资产聚类分析-资产记账分析-支出类型分组数据获取
    List<GroupTextValueVo> getSpendingTypeGroup(String begin_time, String end_time);

    //资产聚类分析-资产记账分析-收入来源分组数据获取
    List<GroupTextValueVo> getIncomeSourceGroup(String begin_time, String end_time);
}
