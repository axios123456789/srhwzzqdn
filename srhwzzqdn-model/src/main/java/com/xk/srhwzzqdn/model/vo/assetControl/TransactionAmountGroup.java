package com.xk.srhwzzqdn.model.vo.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "收支金额分组查询响应实体类")
public class TransactionAmountGroup {
    @Schema(description = "时间")
    private String timeName;

    @Schema(description = "收入金额")
    private BigDecimal invoiceAmount;

    @Schema(description = "支出金额")
    private BigDecimal spendingAmount;

    @Schema(description = "结余金额")
    private BigDecimal balanceAmount;
}
