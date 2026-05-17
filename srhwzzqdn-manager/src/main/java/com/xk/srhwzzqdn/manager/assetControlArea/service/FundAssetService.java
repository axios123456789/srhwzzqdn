package com.xk.srhwzzqdn.manager.assetControlArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.assetControl.FundBaseDateDto;
import com.xk.srhwzzqdn.model.dto.assetControl.FundComm;
import com.xk.srhwzzqdn.model.entity.assetControl.*;

import java.text.ParseException;
import java.util.List;

public interface FundAssetService {
    //获取基金测试数据
    void getFundTestData(String fundCode);

    //获取基金基本数据，基金经理基本数据，关联持仓数据获取
    String getFundBaseDataByCode(String fundCode) throws ParseException;

    //条件分页查询基金基本数据
    PageInfo<FundAsset> getFundBaseDataByConditionAndPage(Integer current, Integer limit, FundBaseDateDto fundBaseDateDto);

    //条件分页查询基金净值数据
    PageInfo<FundNav> getFundNavByConditionAndPage(Integer current, Integer limit, FundComm fundComm);

    //根据基金代码获取基金经理分析数据
    FundManagerAnalysis getFundManagerAnalysisByCode(String fundCode);

    //根据基金代码获取基金持仓数据
    FundHolding getFundHoldingByCode(String fundCode);

    //条件分页查询基金交易与流水数据
    PageInfo<FundTransaction> getFundTransactionByConditionAndPage(Integer current, Integer limit, FundComm fundComm);

    //条件分页查询基金分红数据
    PageInfo<FundDividend> getFundDividendByConditionAndPage(Integer current, Integer limit, FundComm fundComm);

    //根据基金代码获取基金风险收益数据
    List<FundRiskPerformance> getFundRiskPerformance(String fundCode);

    //条件分页获取基金持仓信息
    PageInfo<FundPortfolio> getFundPortfolioByConditionAndPage(Integer current, Integer limit, FundComm fundComm);

    //更新基金资产数据
    void updateFundBaseAsset(FundAsset fundAsset);

    //更新基金经理分析数据
    void updateFundManagerAnalysis(FundManagerAnalysis fundManagerAnalysis);

    //更新基金持仓数据
    void updateFundHolding(FundHolding fundHolding);
}
