package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "基金净值与估值实体类")
public class FundNav {
    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "基金代码")
    private String fundCode;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "最新净值日期")
    private Date navDate;

    @Schema(description = "最新单位净值")
    private BigDecimal unitNav;

    @Schema(description = "累计净值")
    private BigDecimal accumulatedNav;

    @Schema(description = "日涨跌幅（%）")
    private BigDecimal dailyChangeRate;

    @Schema(description = "估值")
    private BigDecimal valuation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private Date updateTime;
}
