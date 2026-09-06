package com.xk.srhwzzqdn.manager.assetControlArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.assetControl.StockQueryDto;
import com.xk.srhwzzqdn.model.entity.assetControl.*;

import java.util.List;

public interface StockAssetService {

    String getStockAllDataByCode(String stockCode) throws Exception;

    String refreshAllStockRealtime();

    StockBasic getStockBasicByCode(String stockCode);

    PageInfo<StockBasic> getStockListByCondition(Integer current, Integer limit, StockQueryDto dto);

    List<StockKline> getStockKline(String stockCode, Integer klineType, Integer limit);

    List<StockFinance> getStockFinance(String stockCode, Integer limit);

    List<StockCapitalFlow> getStockCapitalFlow(String stockCode, Integer limit);

    void updateStockBasic(StockBasic stockBasic);

    void deleteStockDataByCode(String stockCode);

    List<StockBasic> getAllStockBasic();
}