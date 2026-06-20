package com.xk.srhwzzqdn.model.vo.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "业绩分析响应实体类")
@Data
public class PerformanceAnalysisVo {
    @Schema(description = "最大日涨幅（%）")
    private BigDecimal maxDayIncrease;

    @Schema(description = "最大日跌幅（%）")
    private BigDecimal maxDayDecrease;

    @Schema(description = "超10涨幅天数")
    private Integer overTenDayIncrease;

    @Schema(description = "超10跌幅天数")
    private Integer overTenDayDecrease;

    @Schema(description = "超10涨幅合计（%）")
    private BigDecimal overTenDayIncreaseSum;

    @Schema(description = "超10跌幅合计（%）")
    private BigDecimal overTenDayDecreaseSum;

    @Schema(description = "超5涨幅天数")
    private Integer overFiveDayIncrease;

    @Schema(description = "超5跌幅天数")
    private Integer overFiveDayDecrease;

    @Schema(description = "超5涨幅合计（%）")
    private BigDecimal overFiveDayIncreaseSum;

    @Schema(description = "超5跌幅合计（%）")
    private BigDecimal overFiveDayDecreaseSum;

    @Schema(description = "上涨天数")
    private Integer increase;

    @Schema(description = "下跌天数")
    private Integer decrease;

    @Schema(description = "平盘天数")
    private Integer flat;

    @Schema(description = "上涨合计（%）")
    private BigDecimal increaseSum;

    @Schema(description = "下跌合计（%）")
    private BigDecimal decreaseSum;

    @Schema(description = "总涨幅（%）")
    private BigDecimal totalIncrease;

    @Schema(description = "平均涨跌（%）")
    private BigDecimal averageIncrease;
}
