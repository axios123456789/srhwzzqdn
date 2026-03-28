package com.xk.srhwzzqdn.model.vo.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产类型统计 VO
 */
@Data
public class AssetTypeSummaryVO {
    
    @Schema(description = "资产类型")
    private Integer assetType;
    
    @Schema(description = "总金额")
    private BigDecimal totalAmount;
}
