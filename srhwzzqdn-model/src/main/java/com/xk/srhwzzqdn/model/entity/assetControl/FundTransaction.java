package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "基金交易与流水实体类")
public class FundTransaction {
    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "基金代码")
    private String fundCode;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "交易日期（实际交易日）")
    private Date tradeDate;

    @Schema(description = "交易类型（1-申购，2-赎回，3-分红再投，4-转换）")
    private Integer tradeType;

    @Schema(description = "交易份额")
    private BigDecimal tradeShares;

    @Schema(description = "交易金额")
    private BigDecimal tradeAmount;

    @Schema(description = "交易净值（成交时的单位净值）")
    private BigDecimal tradeNav;

    @Schema(description = "交易费用（申购费/赎回费）")
    private BigDecimal tradeFee;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "确认日期")
    private Date confirmDate;

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
