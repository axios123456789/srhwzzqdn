package com.xk.srhwzzqdn.model.vo.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "资产记账返回VO")
public class AssetTransactionVo {
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

    @Schema(description = "记账资产台账id")
    private Integer assetLedgerId;

    @Schema(description = "收支类型（收入/支出）")
    private Integer transactionType;

    @Schema(description = "账单类型")
    private Integer invoiceType;

    @Schema(description = "支出类型")
    private Integer spendingType;

    @Schema(description = "收益类型")
    private Integer incomeType;

    @Schema(description = "结清状态")
    private Integer settlementStatus;

    @Schema(description = "账单所属人")
    private String invoiceOwner;

    @Schema(description = "数据来源")
    private Integer dataSource;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "记账账户名称")
    private String assetName;

    @Schema(description = "记账账户官方码")
    private String assetCode;

    @Schema(description = "记账账户编号")
    private String assetNo;

    @Schema(description = "记账账户类型")
    private String assetType;

    @Schema(description = "记账账户子类型")
    private String assetSubType;

    @Schema(description = "账单所属人")
    private String invoiceOwnerName;
}
