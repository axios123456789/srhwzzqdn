package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "股票财务指标实体类")
public class StockFinance {
    private Integer id;
    private String stockCode;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date reportDate;

    private BigDecimal revenue;
    private BigDecimal revenueYoy;
    private BigDecimal operatingProfit;
    private BigDecimal netProfit;
    private BigDecimal netProfitYoy;
    private BigDecimal deductNetProfit;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
    private BigDecimal accountsReceivable;
    private BigDecimal inventory;
    private BigDecimal prepayment;
    private BigDecimal goodwill;
    private BigDecimal intangibleAssets;
    private BigDecimal fixedAssets;
    private BigDecimal constructionInProgress;
    private BigDecimal shortTermLoan;
    private BigDecimal longTermLoan;
    private BigDecimal accountsPayable;
    private BigDecimal advanceReceipts;
    private BigDecimal operatingCashFlow;
    private BigDecimal investingCashFlow;
    private BigDecimal financingCashFlow;
    private BigDecimal freeCashFlow;
    private BigDecimal grossMargin;
    private BigDecimal netMargin;
    private BigDecimal roe;
    private BigDecimal roa;
    private BigDecimal roic;
    private BigDecimal debtRatio;
    private BigDecimal currentRatio;
    private BigDecimal quickRatio;
    private BigDecimal eps;
    private BigDecimal bps;
    private BigDecimal cashflowPerShare;
    private BigDecimal revenueGrowth1y;
    private BigDecimal profitGrowth1y;
    private BigDecimal assetGrowth;
    private BigDecimal equityGrowth;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}