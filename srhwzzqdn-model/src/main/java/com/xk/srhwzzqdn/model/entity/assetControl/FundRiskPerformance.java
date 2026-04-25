package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "基金风险与绩效指标实体类")
public class FundRiskPerformance {
    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "时间标识（1-近1年，2-近3年，3-近5年）")
    private Integer periodType;

    @Schema(description = "最大回撤（历史最大亏损幅度%）")
    private BigDecimal maxDrawDown;

    @Schema(description = "回测修复天数")
    private Integer recoveryDays;

    @Schema(description = "年化收益率（%）")
    private BigDecimal annualReturn;

    @Schema(description = "夏普比率")
    private BigDecimal sharpeRatio;

    @Schema(description = "波动率（%）")
    private BigDecimal volatility;

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
