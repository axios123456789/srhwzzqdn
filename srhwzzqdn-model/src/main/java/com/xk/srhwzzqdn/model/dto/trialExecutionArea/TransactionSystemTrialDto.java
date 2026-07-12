package com.xk.srhwzzqdn.model.dto.trialExecutionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "交易系统试验条件查询参数")
public class TransactionSystemTrialDto {
    @Schema(description = "交易类型")
    private List<Integer> tradeType;

    @Schema(description = "交易对象名称")
    private String targetName;

    @Schema(description = "计划类型")
    private List<Integer> planType;

    @Schema(description = "计划开始时间（起）")
    private String planStartTime;

    @Schema(description = "计划开始时间（止）")
    private String planEndTime;

    @Schema(description = "交易状态")
    private List<Integer> tradeStatus;

    @Schema(description = "交易结果")
    private List<Integer> tradeResult;

    @Schema(description = "交易失败类型")
    private List<Integer> tradeFailType;

    @Schema(description = "是否触发计划")
    private Integer isUsePlan;

    @Schema(description = "所属人")
    private String owner;
}