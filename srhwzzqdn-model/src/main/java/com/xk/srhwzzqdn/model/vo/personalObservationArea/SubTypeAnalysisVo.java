package com.xk.srhwzzqdn.model.vo.personalObservationArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "资产动态观测-基金/股票子类分析响应实体类")
public class SubTypeAnalysisVo {
    @Schema(description = "资产子类编码")
    private Integer assetSubType;

    @Schema(description = "资产子类名称")
    private String assetSubTypeName;

    @Schema(description = "资产金额")
    private BigDecimal amount;

    @Schema(description = "投入金额")
    private BigDecimal investAmount;

    @Schema(description = "资产数量")
    private Integer assetCount;

    @Schema(description = "已清仓资产金额")
    private BigDecimal clearedAmount;

    @Schema(description = "已清仓投入金额")
    private BigDecimal clearedInvestAmount;

    @Schema(description = "已清仓资产数")
    private Integer clearedCount;

    @Schema(description = "未清仓资产金额")
    private BigDecimal activeAmount;

    @Schema(description = "未清仓投入金额")
    private BigDecimal activeInvestAmount;

    @Schema(description = "未清仓资产数")
    private Integer activeCount;
}
