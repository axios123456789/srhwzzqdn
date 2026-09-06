package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "股票基本信息实体类")
public class StockBasic {
    @Schema(description = "主键自增")
    private Integer id;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "股票名称")
    private String stockName;

    @Schema(description = "市场：1-沪市 0-深市 2-北")
    private Integer market;

    @Schema(description = "所属行业")
    private String industry;

    @Schema(description = "所属板块")
    private String sector;

    @Schema(description = "所属概念板块")
    private String conceptSectors;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "上市日期")
    private Date listDate;

    @Schema(description = "总股本（万股）")
    private BigDecimal totalShares;

    @Schema(description = "流通股本（万股）")
    private BigDecimal circShares;

    @Schema(description = "总市值（亿元）")
    private BigDecimal totalMarketCap;

    @Schema(description = "流通市值（亿元）")
    private BigDecimal circMarketCap;

    @Schema(description = "市盈率(静)")
    private BigDecimal peStatic;

    @Schema(description = "市盈率(动)")
    private BigDecimal peDynamic;

    @Schema(description = "市盈率(TTM)")
    private BigDecimal peTtm;

    @Schema(description = "市净率")
    private BigDecimal pbRatio;

    @Schema(description = "市销率")
    private BigDecimal psRatio;

    @Schema(description = "股息率(%)")
    private BigDecimal dividendYield;

    @Schema(description = "PEG")
    private BigDecimal pegRatio;

    @Schema(description = "EV/EBITDA")
    private BigDecimal evEbitda;

    @Schema(description = "最新价")
    private BigDecimal lastPrice;

    @Schema(description = "涨跌幅(%)")
    private BigDecimal changePct;

    @Schema(description = "涨跌额")
    private BigDecimal changeAmount;

    @Schema(description = "换手率(%)")
    private BigDecimal turnoverRate;

    @Schema(description = "量比")
    private BigDecimal volumeRatio;

    @Schema(description = "振幅(%)")
    private BigDecimal amplitude;

    @Schema(description = "委比(%)")
    private BigDecimal bidRatio;

    @Schema(description = "委差")
    private BigDecimal bidDiff;

    @Schema(description = "内盘")
    private Long innerVolume;

    @Schema(description = "外盘")
    private Long outerVolume;

    @Schema(description = "成交额（万元）")
    private BigDecimal turnover;

    @Schema(description = "成交量（手）")
    private Long volume;

    @Schema(description = "法人代表")
    private String legalRep;

    @Schema(description = "总经理")
    private String generalManager;

    @Schema(description = "注册地址")
    private String registeredAddr;

    @Schema(description = "办公地址")
    private String officeAddr;

    @Schema(description = "公司简介")
    private String companyDesc;

    @Schema(description = "经营范围")
    private String businessScope;

    @Schema(description = "主营业务")
    private String mainBusiness;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建人")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "更新人")
    private String updateBy;
}