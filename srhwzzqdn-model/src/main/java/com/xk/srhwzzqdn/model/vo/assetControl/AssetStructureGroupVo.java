package com.xk.srhwzzqdn.model.vo.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "资产聚类分析-资产台账分析-资产结构响应实体类")
public class AssetStructureGroupVo {
    @Schema(description = "总资产")
    private BigDecimal sumAsset;

    @Schema(description = "储蓄资产")
    private BigDecimal savingAsset;

    @Schema(description = "投资资产")
    private BigDecimal investAsset;
}
