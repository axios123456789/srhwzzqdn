package com.xk.srhwzzqdn.manager.assetControlArea.service;

import java.text.ParseException;

public interface FundAssetService {
    //获取基金测试数据
    void getFundTestData(String fundCode);

    //获取基金基本数据，基金经理基本数据，关联持仓数据获取
    void getFundBaseDataByCode(String fundCode) throws ParseException;
}
