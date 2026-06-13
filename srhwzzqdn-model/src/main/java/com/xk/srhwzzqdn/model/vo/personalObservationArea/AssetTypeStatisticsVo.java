package com.xk.srhwzzqdn.model.vo.personalObservationArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "资产动态观测-资产类型分组统计响应实体类")
public class AssetTypeStatisticsVo {
    @Schema(description = "资产类型编码")
    private Integer assetType;

    @Schema(description = "资产类型名称")
    private String assetTypeName;

    @Schema(description = "资产金额")
    private BigDecimal amount;

    @Schema(description = "投入金额")
    private BigDecimal investAmount;

    @Schema(description = "资产数量")
    private Integer assetCount;
}
