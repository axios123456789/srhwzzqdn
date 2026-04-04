package com.xk.srhwzzqdn.model.dto.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "资产记账业务查询条件类")
public class AssetTransactionDto {
    @Schema(description = "账单编号")
    private String invoiceNo;

    @Schema(description = "账单行为")
    private String invoiceAction;

    @Schema(description = "记账时间（开始）")
    private String recordTimeStart;

    @Schema(description = "记账时间（结束）")
    private String recordTimeEnd;

    @Schema(description = "金额（大于）")
    private BigDecimal amount;

    @Schema(description = "记账资产台账 id")
    private Integer assetLedgerId;

    @Schema(description = "收支类型（收入/支出）")
    private Integer transactionType;

    @Schema(description = "账单类型")
    private List<Integer> invoiceType;

    @Schema(description = "支出类型（必要、需要、想要）")
    private List<Integer> spendingType;

    @Schema(description = "收益类型（劳动报酬、资产增值）")
    private List<Integer> incomeType;

    @Schema(description = "结清状态（已结清、待结清）")
    private Integer settlementStatus;

    @Schema(description = "账单所属人")
    private String invoiceOwner;

    @Schema(description = "数据来源（手动录入/生活记忆）")
    private List<Integer> dataSource;
}
