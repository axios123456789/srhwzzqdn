package com.xk.srhwzzqdn.model.dto.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "股票条件查询类")
public class StockQueryDto {
    @Schema(description = "股票名称")
    private String stockName;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "所属行业")
    private String industry;

    @Schema(description = "市场：1-沪市 0-深市 2-北")
    private Integer market;
}