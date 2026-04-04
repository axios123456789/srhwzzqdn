package com.xk.srhwzzqdn.model.vo.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "资产记账类型分组VO")
public class AssetTransactionTypeGroupVo {
    @Schema(description = "收支类型")
    private String transactionType;

    @Schema(description = "收支金额")
    private BigDecimal transactionAmount;
}
