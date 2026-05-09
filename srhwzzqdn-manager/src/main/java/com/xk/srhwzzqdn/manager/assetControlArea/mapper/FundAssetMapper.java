package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.entity.assetControl.FundAsset;
import com.xk.srhwzzqdn.model.entity.assetControl.FundHolding;
import com.xk.srhwzzqdn.model.entity.assetControl.FundManagerAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
