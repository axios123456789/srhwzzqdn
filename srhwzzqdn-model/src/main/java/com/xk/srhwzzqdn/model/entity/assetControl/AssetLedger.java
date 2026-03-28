package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "资产台账实体类")
public class AssetLedger {
    @Schema(description = "资产台账id")
    private Integer id;

    @Schema(description = "资产名称（余额宝/微信/xxx银行卡/xxx基金）")
    private String assetName;

    @Schema(description = "资产官方标识（如账号/基金代码")
    private String assetCode;

    @Schema(description = "资产编号（系统内部唯一编号）")
    private String assetNo;

    @Schema(description = "资产类型（储蓄/保险/基金/股票）")
    private Integer assetType;

    @Schema(description = "资产子类（如银行卡储蓄/指数基金）")
    private Integer assetSubType;

    @Schema(description = "资产金额")
    private BigDecimal amount;

    @Schema(description = "投入金额（理财类）")
    private BigDecimal investAmount;

    @Schema(description = "资产状态（正常/冻结/已赎回/已清仓）")
    private Integer assetStatus;

    @Schema(description = "资产备注")
    private String remark;

    @Schema(description = "资产所属人")
    private String assetOwner;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private String updateBy;
}
