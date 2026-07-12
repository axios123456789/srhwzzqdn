package com.xk.srhwzzqdn.model.entity.trialExecutionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "交易规则实体类")
public class TransactionRule {
    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "规则编号")
    private String ruleCode;

    @Schema(description = "规则类型：1-思想规则 2-选股规则 3-入场规则 4-止损规则 5-止盈规则 6-仓位规则")
    private Integer ruleType;

    @Schema(description = "规则内容")
    private String ruleContent;

    @Schema(description = "规则细节")
    private String ruleDetail;

    @Schema(description = "规则排序")
    private Integer sortOrder;

    @Schema(description = "规则状态：1-正常 0-作废")
    private Integer ruleStatus;

    @Schema(description = "规则制定时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "规则使用次数")
    private Integer useCount;

    @Schema(description = "规则违反次数")
    private Integer violateCount;

    @Schema(description = "遵守规则成功次数")
    private Integer complySuccessCount;

    @Schema(description = "违反规则成功次数")
    private Integer violateSuccessCount;

    @Schema(description = "违反规则惩罚")
    private String violatePenalty;

    @Schema(description = "所属人")
    private String owner;
}