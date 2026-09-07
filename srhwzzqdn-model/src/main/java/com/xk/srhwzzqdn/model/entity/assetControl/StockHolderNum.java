package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "股东人数历史实体类")
public class StockHolderNum {
    @Schema(description = "主键自增")
    private Integer id;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "股票名称")
    private String stockName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "股东户数截止日期")
    private Date endDate;

    @Schema(description = "股东户数")
    private Long holderNum;

    @Schema(description = "上期股东户数")
    private Long preHolderNum;

    @Schema(description = "较上期变化")
    private Long holderNumChange;

    @Schema(description = "较上期变化率(%)")
    private BigDecimal holderNumRatio;

    @Schema(description = "户均持股市值(元)")
    private BigDecimal avgMarketCap;

    @Schema(description = "户均持股数(股)")
    private BigDecimal avgHoldNum;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "公告日期")
    private Date noticeDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private Date createTime;
}
