package com.xk.srhwzzqdn.manager.assetControlArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.assetControl.FundBaseDateDto;
import com.xk.srhwzzqdn.model.dto.assetControl.FundComm;
import com.xk.srhwzzqdn.model.entity.assetControl.FundAsset;
import com.xk.srhwzzqdn.model.entity.assetControl.FundManagerAnalysis;
import com.xk.srhwzzqdn.model.entity.assetControl.FundNav;

import java.text.ParseException;

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
}
