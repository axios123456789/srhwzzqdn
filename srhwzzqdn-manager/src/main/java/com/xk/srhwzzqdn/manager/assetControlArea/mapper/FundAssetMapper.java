package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.dto.assetControl.FundBaseDateDto;
import com.xk.srhwzzqdn.model.dto.assetControl.FundComm;
import com.xk.srhwzzqdn.model.entity.assetControl.*;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FundAssetMapper {
    //根据基金代码查询基金的数据是否已获取
    @Select("select count(*) from t_fund_asset where fund_code = #{param1}")
    int isExistByCode(String fundCode);

    //基金基本数据入库
    void addFundAsset(FundAsset fundAsset);

    //基金经理分析数据入库
    void addFundManagerAnalysis(FundManagerAnalysis fundManagerAnalysis);

    //持仓情况入库
    void addFundHolding(FundHolding fundHolding);

    //条件查询基金基本数据列表
    List<FundAsset> getFundBaseDataByCondition(FundBaseDateDto fundBaseDateDto);

    //条件查询基金净值数据列表
    List<FundNav> getFundNavByCondition(FundComm fundComm);

    //根据基金代码获取基金经理分析数据
    FundManagerAnalysis getFundManagerAnalysisByCode(String fundCode);

    //根据基金代码获取持仓情况数据
    FundHolding getFundHoldingByCode(String fundCode);

    //条件查询基金交易数据列表
    List<FundTransaction> getFundTransactionByCondition(FundComm fundComm);

    //条件查询基金分红数据列表
    List<FundDividend> getFundDividendByCondition(FundComm fundComm);

    //条件查询基金风险收益数据列表
    List<FundRiskPerformance> getFundRiskPerformance(String fundCode);

    //条件查询基金持仓数据列表
    List<FundPortfolio> getFundPortfolioByCondition(FundComm fundComm);

    //更新基金基本数据
    void updateFundBaseAsset(FundAsset fundAsset);

    //更新基金经理分析数据
    void updateFundManagerAnalysis(FundManagerAnalysis fundManagerAnalysis);

    //更新持仓情况数据
    void updateFundHolding(FundHolding fundHolding);

    //添加基金净值数据
    void addFundNav(FundNav fundNav);

    //更新基金净值数据
    void updateFundNav(FundNav fundNav);

    //根据id删除基金净值数据
    @Delete("delete from t_fund_nav where id = #{param1}")
    void deleteFundNav(Long id);
}
