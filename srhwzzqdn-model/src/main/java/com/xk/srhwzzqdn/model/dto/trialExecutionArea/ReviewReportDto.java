package com.xk.srhwzzqdn.model.dto.trialExecutionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "复盘分析报表查询参数")
public class ReviewReportDto {
    @Schema(description = "统计开始时间")
    private String startTime;

    @Schema(description = "统计结束时间")
    private String endTime;

    @Schema(description = "所属人")
    private String owner;
}