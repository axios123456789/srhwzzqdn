package com.xk.srhwzzqdn.model.dto.trialExecutionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "每日复盘查询参数")
public class DailyReviewDto {
    @Schema(description = "复盘日期开始")
    private String reviewDateStart;

    @Schema(description = "复盘日期结束")
    private String reviewDateEnd;

    @Schema(description = "市场状态（多选）")
    private List<Integer> marketStatus;

    @Schema(description = "情绪温度（多选）")
    private List<Integer> emotionTemp;

    @Schema(description = "适配体系（多选）")
    private List<Integer> adaptSystem;

    @Schema(description = "操作自评（多选）")
    private List<Integer> operationSelfRating;

    @Schema(description = "所属人")
    private String owner;
}