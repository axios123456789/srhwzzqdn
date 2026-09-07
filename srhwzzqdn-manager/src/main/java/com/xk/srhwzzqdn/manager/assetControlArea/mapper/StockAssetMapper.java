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

    @Delete("delete from t_stock_news where stock_code = #{param1}")
    void deleteStockNewsByCode(String stockCode);

    @Delete("delete from t_stock_holder_num where stock_code = #{param1}")
    void deleteStockHolderNumByCode(String stockCode);

    void batchAddStockKline(@Param("list") List<StockKline> list);

    void batchAddStockFinance(@Param("list") List<StockFinance> list);

    void batchAddStockCapitalFlow(@Param("list") List<StockCapitalFlow> list);

    void batchAddStockNews(@Param("list") List<StockNews> list);

    @Select("select * from t_stock_news where stock_code = #{param1} order by publish_time desc limit #{param2}")
    List<StockNews> getStockNews(String stockCode, Integer limit);

    // insert ignore 依赖唯一索引 uk_code_date(stock_code, end_date) 去重，股东户数历史增量保留
    void batchAddStockHolderNum(@Param("list") List<StockHolderNum> list);

    @Select("select * from t_stock_holder_num where stock_code = #{param1} order by end_date desc limit #{param2}")
    List<StockHolderNum> getStockHolderNum(String stockCode, Integer limit);

    @Select("select * from t_stock_kline where stock_code = #{param1} and kline_type = #{param2} order by trade_date desc limit #{param3}")
    List<StockKline> getStockKline(String stockCode, Integer klineType, Integer limit);

    @Select("select * from t_stock_finance where stock_code = #{param1} order by report_date desc limit #{param2}")
    List<StockFinance> getStockFinance(String stockCode, Integer limit);

    @Select("select * from t_stock_capital_flow where stock_code = #{param1} order by trade_date desc limit #{param2}")
    List<StockCapitalFlow> getStockCapitalFlow(String stockCode, Integer limit);

    @Select("select * from t_stock_basic order by update_time desc")
    List<StockBasic> getAllStockBasic();
}