package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.dto.assetControl.FundBaseDateDto;
import com.xk.srhwzzqdn.model.dto.assetControl.FundComm;
import com.xk.srhwzzqdn.model.entity.assetControl.FundAsset;
import com.xk.srhwzzqdn.model.entity.assetControl.FundHolding;
import com.xk.srhwzzqdn.model.entity.assetControl.FundManagerAnalysis;
import com.xk.srhwzzqdn.model.entity.assetControl.FundNav;
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
}
