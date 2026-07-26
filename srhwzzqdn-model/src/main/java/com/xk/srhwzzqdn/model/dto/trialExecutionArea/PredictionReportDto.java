package com.xk.srhwzzqdn.model.dto.trialExecutionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "预测统计报表查询参数")
public class PredictionReportDto {
    @Schema(description = "统计开始时间")
    private String startTime;

    @Schema(description = "统计结束时间")
    private String endTime;

    @Schema(description = "依据类型（多选查询，使用FIND_IN_SET匹配）")
    private List<String> basisType;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "所属人")
    private String owner;

    // ====================穿透明细查询参数====================
    @Schema(description = "预测结果（1-成功 2-失败），用于穿透明细")
    private Integer predictionResult;

    @Schema(description = "预测情况，用于穿透明细")
    private Integer predictionSituation;

    @Schema(description = "模拟操作（1-买入 2-卖出），用于穿透明细")
    private Integer simulateOperation;

    @Schema(description = "月份（格式：YYYY-MM），用于穿透明细")
    private String month;

    @Schema(description = "明细股票代码，用于穿透明细（与统计条件stockCode区分）")
    private String detailStockCode;
}
