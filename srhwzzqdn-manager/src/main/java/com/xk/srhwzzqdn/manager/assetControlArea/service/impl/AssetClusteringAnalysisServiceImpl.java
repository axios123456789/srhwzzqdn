package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.xk.srhwzzqdn.manager.assetControlArea.mapper.AssetClusteringAnalysisMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetClusteringAnalysisService;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetStructureGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.InvestmentReturnAnalysisGroupVo;
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
}
