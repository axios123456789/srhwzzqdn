package com.xk.srhwzzqdn.model.dto.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "基金基本数据条件查询类")
public class FundBaseDateDto {
    @Schema(description = "基金名称")
    private String fundName;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "基金类型")
    private List<Integer> fundType;

    @Schema(description = "资产规模")
    private BigDecimal assetScale;

    @Schema(description = "运作方式")
    private Integer operationMode;

    @Schema(description = "封闭期")
    private Integer closedEndDays;
}
