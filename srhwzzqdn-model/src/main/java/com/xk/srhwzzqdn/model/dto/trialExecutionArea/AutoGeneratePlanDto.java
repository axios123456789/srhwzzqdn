package com.xk.srhwzzqdn.model.dto.trialExecutionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "自动生成交易计划参数")
public class AutoGeneratePlanDto {
    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "交易类型：1-股票买入 2-股票卖出")
    private Integer tradeType;

    @Schema(description = "计划类型：1-止盈 2-止损 3-建仓")
    private Integer planType;
}