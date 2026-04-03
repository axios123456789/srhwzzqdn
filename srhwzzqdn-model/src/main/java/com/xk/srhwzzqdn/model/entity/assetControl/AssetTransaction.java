package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "资产记账业务实体类")
public class AssetTransaction {
    @Schema(description = "主键（自增）")
    private Integer id;

    @Schema(description = "账单编号")
    private String invoiceNo;

    @Schema(description = "账单行为")
    private String invoiceAction;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "记账时间")
    private Date recordTime;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "记账资产台账 id")
    private Integer assetLedgerId;

    @Schema(description = "收支类型（收入/支出）")
    private Integer transactionType;

    @Schema(description = "账单类型（挣钱软件佣金、兼职、理财收益、饮食支出、房租、日用品开销、工资）")
    private Integer invoiceType;

    @Schema(description = "支出类型（必要、需要、想要）")
    private Integer spendingType;

    @Schema(description = "收益类型（劳动报酬、资产增值）")
    private Integer incomeType;

    @Schema(description = "结清状态（已结清、待结清）")
    private Integer settlementStatus;

    @Schema(description = "账单所属人")
    private String invoiceOwner;

    @Schema(description = "数据来源（手动录入/生活记忆）")
    private Integer dataSource;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "更新人")
    private String updateBy;
}
