package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "股票资金流向实体类")
public class StockCapitalFlow {
    private Integer id;
    private String stockCode;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date tradeDate;

    private BigDecimal mainNetInflow;
    private BigDecimal mainNetPct;
    private BigDecimal superLargeNet;
    private BigDecimal superLargeInflow;
    private BigDecimal superLargeOutflow;
    private BigDecimal largeNet;
    private BigDecimal largeInflow;
    private BigDecimal largeOutflow;
    private BigDecimal mediumNet;
    private BigDecimal smallNet;
    private BigDecimal main3d;
    private BigDecimal main5d;
    private BigDecimal main10d;
    private BigDecimal main20d;
    private String flowLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}