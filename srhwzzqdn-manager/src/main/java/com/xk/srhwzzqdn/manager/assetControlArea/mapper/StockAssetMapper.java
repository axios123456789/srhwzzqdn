package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.dto.assetControl.StockQueryDto;
import com.xk.srhwzzqdn.model.entity.assetControl.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StockAssetMapper {

    @Select("select count(*) from t_stock_basic where stock_code = #{param1}")
    int isExistByCode(String stockCode);

    @Select("select * from t_stock_basic where stock_code = #{param1}")
    StockBasic getStockBasicByCode(String stockCode);

    void addStockBasic(StockBasic stockBasic);

    void updateStockBasic(StockBasic stockBasic);

    void updateStockRealtime(StockBasic stockBasic);

    List<StockBasic> getStockListByCondition(StockQueryDto dto);

    @Delete("delete from t_stock_basic where stock_code = #{param1}")
    void deleteStockBasicByCode(String stockCode);

    @Delete("delete from t_stock_kline where stock_code = #{param1}")
    void deleteStockKlineByCode(String stockCode);

    @Delete("delete from t_stock_finance where stock_code = #{param1}")
    void deleteStockFinanceByCode(String stockCode);

    @Delete("delete from t_stock_capital_flow where stock_code = #{param1}")
    void deleteStockCapitalFlowByCode(String stockCode);

    void batchAddStockKline(@Param("list") List<StockKline> list);

    void batchAddStockFinance(@Param("list") List<StockFinance> list);

    void batchAddStockCapitalFlow(@Param("list") List<StockCapitalFlow> list);

    @Select("select * from t_stock_kline where stock_code = #{param1} and kline_type = #{param2} order by trade_date desc limit #{param3}")
    List<StockKline> getStockKline(String stockCode, Integer klineType, Integer limit);

    @Select("select * from t_stock_finance where stock_code = #{param1} order by report_date desc limit #{param2}")
    List<StockFinance> getStockFinance(String stockCode, Integer limit);

    @Select("select * from t_stock_capital_flow where stock_code = #{param1} order by trade_date desc limit #{param2}")
    List<StockCapitalFlow> getStockCapitalFlow(String stockCode, Integer limit);

    @Select("select * from t_stock_basic order by update_time desc")
    List<StockBasic> getAllStockBasic();
}