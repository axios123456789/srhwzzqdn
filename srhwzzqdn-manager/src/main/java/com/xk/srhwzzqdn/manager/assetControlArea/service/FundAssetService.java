package com.xk.srhwzzqdn.manager.assetControlArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.assetControl.FundBaseDateDto;
import com.xk.srhwzzqdn.model.dto.assetControl.FundComm;
import com.xk.srhwzzqdn.model.entity.assetControl.*;
import com.xk.srhwzzqdn.model.vo.assetControl.NavEChartsVo;

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

    //添加基金净值数据
    void addFundNav(FundNav fundNav);

    //更新基金净值数据
    void updateFundNav(FundNav fundNav);

    //根据id删除基金净值数据
    void deleteFundNav(Long id);

    //添加基金交易数据
    void addFundTransaction(FundTransaction fundTransaction);

    //更新基金交易数据
    void updateFundTransaction(FundTransaction fundTransaction);

    //根据id删除基金交易数据
    void deleteFundTransaction(Long id);

    //添加基金分红数据
    void addFundDividend(FundDividend fundDividend);

    //更新基金分红数据
    void updateFundDividend(FundDividend fundDividend);

    //根据id删除基金分红数据
    void deleteFundDividend(Long id);

    //添加基金风险绩效数据
    void addFundRiskPerformance(FundRiskPerformance fundRiskPerformance);

    //更新基金风险绩效数据
    void updateFundRiskPerformance(FundRiskPerformance fundRiskPerformance);

    //根据id删除基金风险绩效数据
    void deleteFundRiskPerformance(Long id);

    //添加基金持仓信息数据
    void addFundPortfolio(FundPortfolio fundPortfolio);

    //更新基金持仓信息数据
    void updateFundPortfolio(FundPortfolio fundPortfolio);

    //根据id删除基金持仓信息数据
    void deleteFundPortfolio(Long id);

    //获取业绩走势echarts数据
    List<NavEChartsVo> getNaveChartsByCondition(FundComm fundComm);

    //根据基金代码删除基金的全部数据
    void deleteFundDataByCode(String fund_code);

    //根据基金代码批量删除基金的全部数据
    void deleteFundDataByCodes(List<String> fund_codes);

    //获取基金重要数据 -> 基金持仓数据插入数据库，基金净值数据插入数据库
    void addFundImportData(String fundCode);
}
