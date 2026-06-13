package com.xk.srhwzzqdn.manager.personalObservationArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.personalObservationArea.mapper.AssetDynamicsObservationMapper;
import com.xk.srhwzzqdn.manager.personalObservationArea.service.AssetDynamicsObservationService;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerPageVo;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerVo;
import com.xk.srhwzzqdn.model.vo.common.GroupTextValueVo;
import com.xk.srhwzzqdn.model.vo.personalObservationArea.AssetTypeStatisticsVo;
import com.xk.srhwzzqdn.model.vo.personalObservationArea.SubTypeAnalysisVo;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssetDynamicsObservationServiceImpl implements AssetDynamicsObservationService {
    @Autowired
    private AssetDynamicsObservationMapper assetDynamicsObservationMapper;

    private String getCurrentUserId() {
        return AuthContextUtil.get().getId();
    }

    @Override
    public List<AssetTypeStatisticsVo> getAssetTypeStatistics() {
        List<AssetTypeStatisticsVo> list = assetDynamicsObservationMapper.getAssetTypeStatistics(getCurrentUserId());
        // 追加合计行
        AssetTypeStatisticsVo total = new AssetTypeStatisticsVo();
        total.setAssetType(-1);
        total.setAssetTypeName("合计");
        total.setAmount(list.stream().map(AssetTypeStatisticsVo::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        total.setInvestAmount(list.stream().map(AssetTypeStatisticsVo::getInvestAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        total.setAssetCount(list.stream().mapToInt(AssetTypeStatisticsVo::getAssetCount).sum());
        list.add(total);
        return list;
    }

    @Override
    public List<SubTypeAnalysisVo> getFundSubTypeAnalysis() {
        List<SubTypeAnalysisVo> list = assetDynamicsObservationMapper.getFundSubTypeAnalysis(getCurrentUserId());
        addSubTypeSummaryRow(list);
        return list;
    }

    @Override
    public List<SubTypeAnalysisVo> getStockSubTypeAnalysis() {
        List<SubTypeAnalysisVo> list = assetDynamicsObservationMapper.getStockSubTypeAnalysis(getCurrentUserId());
        addSubTypeSummaryRow(list);
        return list;
    }

    /**
     * 为基金/股票子类分析列表追加合计行
     */
    private void addSubTypeSummaryRow(List<SubTypeAnalysisVo> list) {
        SubTypeAnalysisVo total = new SubTypeAnalysisVo();
        total.setAssetSubType(-1);
        total.setAssetSubTypeName("合计");
        total.setAmount(list.stream().map(SubTypeAnalysisVo::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        total.setInvestAmount(list.stream().map(SubTypeAnalysisVo::getInvestAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        total.setAssetCount(list.stream().mapToInt(SubTypeAnalysisVo::getAssetCount).sum());
        total.setClearedAmount(list.stream().map(SubTypeAnalysisVo::getClearedAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        total.setClearedInvestAmount(list.stream().map(SubTypeAnalysisVo::getClearedInvestAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        total.setClearedCount(list.stream().mapToInt(SubTypeAnalysisVo::getClearedCount).sum());
        total.setActiveAmount(list.stream().map(SubTypeAnalysisVo::getActiveAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        total.setActiveInvestAmount(list.stream().map(SubTypeAnalysisVo::getActiveInvestAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        total.setActiveCount(list.stream().mapToInt(SubTypeAnalysisVo::getActiveCount).sum());
        list.add(total);
    }

    @Override
    public AssetLedgerPageVo getAssetDetailByType(Integer assetType, Integer page, Integer limit) {
        // 合计行传入-1，转为null表示不筛选该字段
        if (assetType != null && assetType == -1) {
            assetType = null;
        }
        PageHelper.startPage(page, limit);
        List<AssetLedgerVo> list = assetDynamicsObservationMapper.getAssetDetailByType(getCurrentUserId(), assetType);
        PageInfo<AssetLedgerVo> pageInfo = new PageInfo<>(list);

        AssetLedgerPageVo pageVo = new AssetLedgerPageVo();
        pageVo.setRecords(pageInfo.getList());
        pageVo.setTotal(pageInfo.getTotal());
        pageVo.setCurrent(pageInfo.getPageNum());
        pageVo.setSize(pageInfo.getPageSize());
        return pageVo;
    }

    @Override
    public AssetLedgerPageVo getAssetDetailBySubTypeAndStatus(Integer assetType, Integer assetSubType, Boolean isCleared, Integer page, Integer limit) {
        // 合计行传入-1，转为null表示不筛选该字段
        if (assetType != null && assetType == -1) {
            assetType = null;
        }
        if (assetSubType != null && assetSubType == -1) {
            assetSubType = null;
        }
        PageHelper.startPage(page, limit);
        List<AssetLedgerVo> list = assetDynamicsObservationMapper.getAssetDetailBySubTypeAndStatus(getCurrentUserId(), assetType, assetSubType, isCleared);
        PageInfo<AssetLedgerVo> pageInfo = new PageInfo<>(list);

        AssetLedgerPageVo pageVo = new AssetLedgerPageVo();
        pageVo.setRecords(pageInfo.getList());
        pageVo.setTotal(pageInfo.getTotal());
        pageVo.setCurrent(pageInfo.getPageNum());
        pageVo.setSize(pageInfo.getPageSize());
        return pageVo;
    }

    @Override
    public List<GroupTextValueVo> getAssetTypeDistribution() {
        return assetDynamicsObservationMapper.getAssetTypeDistribution(getCurrentUserId());
    }

    @Override
    public List<GroupTextValueVo> getSubTypeDistribution(Integer assetType) {
        return assetDynamicsObservationMapper.getSubTypeDistribution(getCurrentUserId(), assetType);
    }

    @Override
    public List<GroupTextValueVo> getClearedAssetProfit() {
        return assetDynamicsObservationMapper.getClearedAssetProfit(getCurrentUserId());
    }
}
