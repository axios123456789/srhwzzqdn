package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.xk.srhwzzqdn.manager.assetControlArea.mapper.AssetClusteringAnalysisMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetClusteringAnalysisService;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetStructureGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.InvestmentReturnAnalysisGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.TransactionAmountGroup;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetClusteringAnalysisServiceImpl implements AssetClusteringAnalysisService {
    @Autowired
    private AssetClusteringAnalysisMapper assetClusteringAnalysisMapper;

    /**
     * 资产聚类分析-资产台账分析-资产结构(总资产，储蓄资产，投资资产)数据获取
     * @return
     */
    @Override
    public AssetStructureGroupVo getAssetStructure() {
        return assetClusteringAnalysisMapper.getAssetStructure(AuthContextUtil.get().getId());
    }

    /**
     * 资产聚类分析-资产台账分析-资产类型分组数据获取
     * @return
     */
    @Override
    public List<GroupTextValueVo> getAssetTypeGroup() {
        return assetClusteringAnalysisMapper.getAssetTypeGroup(AuthContextUtil.get().getId());
    }

    /**
     * 资产聚类分析-资产台账分析-资产状态分布分组数据获取
     * @return
     */
    @Override
    public List<GroupTextValueVo> getAssetStatusGroup() {
        return assetClusteringAnalysisMapper.getAssetStatusGroup(AuthContextUtil.get().getId());
    }

    /**
     * 资产聚类分析-资产台账分析-资产金额排行数据获取
     * @return
     */
    @Override
    public List<GroupTextValueVo> getAssetAmountRank() {
        return assetClusteringAnalysisMapper.getAssetAmountRank(AuthContextUtil.get().getId());
    }

    /**
     * 资产聚类分析-资产台账分析-投资收益分析数据获取
     * @return
     */
    @Override
    public List<InvestmentReturnAnalysisGroupVo> getInvestmentReturnAnalysis() {
        return assetClusteringAnalysisMapper.getInvestmentReturnAnalysis(AuthContextUtil.get().getId());
    }

    /**
     * 资产聚类分析-资产记账分析-收支金额分组数据获取
     * @param begin_time
     * @param end_time
     * @return
     */
    @Override
    public List<TransactionAmountGroup> getTransactionAmountGroup(String begin_time, String end_time) {
        return assetClusteringAnalysisMapper.getTransactionAmountGroup(begin_time, end_time, AuthContextUtil.get().getId());
    }

    /**
     * 资产聚类分析-资产记账分析-支出结构分组数据获取
     * @param begin_time
     * @param end_time
     * @return
     */
    @Override
    public List<GroupTextValueVo> getExpenseStructureGroup(String begin_time, String end_time) {
        return assetClusteringAnalysisMapper.getExpenseStructureGroup(begin_time, end_time, AuthContextUtil.get().getId());
    }

    /**
     * 资产聚类分析-资产记账分析-支出类型分组数据获取
     * @param begin_time
     * @param end_time
     * @return
     */
    @Override
    public List<GroupTextValueVo> getSpendingTypeGroup(String begin_time, String end_time) {
        return assetClusteringAnalysisMapper.getSpendingTypeGroup(begin_time, end_time, AuthContextUtil.get().getId());
    }

    /**
     * 资产聚类分析-资产记账分析-收入来源分组数据获取
     * @param begin_time
     * @param end_time
     * @return
     */
    @Override
    public List<GroupTextValueVo> getIncomeSourceGroup(String begin_time, String end_time) {
        return assetClusteringAnalysisMapper.getIncomeSourceGroup(begin_time, end_time, AuthContextUtil.get().getId());
    }
}
