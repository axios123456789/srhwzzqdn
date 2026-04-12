package com.xk.srhwzzqdn.model.vo.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "投资收益分析响应实体类")
public class InvestmentReturnAnalysisGroupVo {
    @Schema(description = "资产名称")
    private String assetName;

    @Schema(description = "资产金额")
    private BigDecimal assetAmount;

    @Schema(description = "投资金额")
    private BigDecimal investAmount;
}
