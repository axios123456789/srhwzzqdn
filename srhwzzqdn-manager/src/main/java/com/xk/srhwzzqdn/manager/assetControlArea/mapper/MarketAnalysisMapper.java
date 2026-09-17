package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.entity.assetControl.MarketAnalysisDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface MarketAnalysisMapper {

    /**
     * 市场分析宽表不存在时自动建表（首次调用时执行，幂等）
     */
    void createTableIfNotExists();

    /**
     * 删除指定交易日的分析记录（重分析当天时先删后插，仅限当天，不动其他交易日）
     */
    void deleteByMarketDate(@Param("marketDate") Date marketDate);

    /**
     * 插入一条市场分析记录
     */
    void insertAnalysis(MarketAnalysisDaily record);

    /**
     * 查询指定交易日的最新分析记录
     */
    MarketAnalysisDaily selectByMarketDate(@Param("marketDate") Date marketDate);

    /**
     * 查询最近N天的分析记录（按日期倒序），供AI历史对比复盘使用
     */
    List<MarketAnalysisDaily> selectRecent(@Param("limit") Integer limit);
}
