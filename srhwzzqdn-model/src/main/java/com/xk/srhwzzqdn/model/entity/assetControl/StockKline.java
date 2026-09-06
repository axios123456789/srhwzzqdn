package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "股票K线数据实体类")
public class StockKline {
    private Integer id;
    private String stockCode;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date tradeDate;

    @Schema(description = "K线类型：1-日 2-周 3-月")
    private Integer klineType;

    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Long volume;
    private BigDecimal turnover;
    private BigDecimal amplitude;
    private BigDecimal changePct;
    private BigDecimal changeAmount;
    private BigDecimal turnoverRate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}