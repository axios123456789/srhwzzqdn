package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.xk.srhwzzqdn.manager.assetControlArea.mapper.AssetClusteringAnalysisMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetClusteringAnalysisService;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetStructureGroupVo;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;
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
        return assetClusteringAnalysisMapper.getAssetStructure();
    }

    /**
     * 资产聚类分析-资产台账分析-资产类型分组数据获取
     * @return
     */
    @Override
    public List<GroupTextValueVo> getAssetTypeGroup() {
        return assetClusteringAnalysisMapper.getAssetTypeGroup();
    }
}
