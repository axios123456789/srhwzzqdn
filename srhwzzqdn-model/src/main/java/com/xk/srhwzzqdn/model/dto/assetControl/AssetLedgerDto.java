package com.xk.srhwzzqdn.model.dto.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "资产台账查询条件类")
public class AssetLedgerDto {
    @Schema(description = "资产名称")
    private String assetName;

    @Schema(description = "资产编号")
    private String assetNo;

    @Schema(description = "资产类型")
    private Integer assetType;

    @Schema(description = "资产子类")
    private List<Integer> assetSubType;

    @Schema(description = "资产金额（大于）")
    private BigDecimal amount;

    private String assetOwner;

    @Schema(description = "资产状态")
    private List<Integer> assetStatus;
}
