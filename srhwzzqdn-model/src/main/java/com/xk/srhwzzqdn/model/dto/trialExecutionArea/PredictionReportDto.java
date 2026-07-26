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

    @Schema(description = "依据类型")
    private List<Integer> basisType;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "所属人")
    private String owner;
}
