package com.xk.srhwzzqdn.model.vo.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "资产台账分页返回VO类")
public class AssetLedgerPageVo {
    @Schema(description = "资产台账列表")
    private List<AssetLedgerVo> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页")
    private Integer current;

    @Schema(description = "每页大小")
    private Integer size;

    @Schema(description = "资产类型分组统计金额")
    private Map<Integer, BigDecimal> assetTypeSummary;
}
