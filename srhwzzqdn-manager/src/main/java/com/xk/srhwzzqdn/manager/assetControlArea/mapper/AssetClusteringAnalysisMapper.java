package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.vo.assetControl.AssetStructureGroupVo;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AssetClusteringAnalysisMapper {
    //资产结构(总资产，储蓄资产，投资资产)数据获取
    AssetStructureGroupVo getAssetStructure();

    //资产聚类分析-资产台账分析-资产类型分组数据获取
    List<GroupTextValueVo> getAssetTypeGroup();
}
