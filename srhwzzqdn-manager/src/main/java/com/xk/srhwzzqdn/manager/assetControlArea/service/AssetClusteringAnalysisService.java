package com.xk.srhwzzqdn.manager.assetControlArea.service;

import com.xk.srhwzzqdn.model.vo.assetControl.AssetStructureGroupVo;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;

import java.util.List;

public interface AssetClusteringAnalysisService {
    //资产聚类分析-资产台账分析-资产结构(总资产，储蓄资产，投资资产)数据获取
    AssetStructureGroupVo getAssetStructure();

    //资产聚类分析-资产台账分析-资产类型分组数据获取
    List<GroupTextValueVo> getAssetTypeGroup();
}
