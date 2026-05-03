package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "基金经理分析实体类")
public class FundManagerAnalysis {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "基金经理名称")
    private String managerName;

    @Schema(description = "星级（1-5星）")
    private BigDecimal starRating;

    @Schema(description = "从业时间（年）")
    private BigDecimal workTime;

    @Schema(description = "管理规模（亿）")
    private BigDecimal manageScale;

    @Schema(description = "综合评分（0-100）")
    private BigDecimal totalScore;

    @Schema(description = "选证能力评分")
    private BigDecimal stockSelectScore;

    @Schema(description = "收益率评分")
    private BigDecimal returnScore;

    @Schema(description = "抗风险评分")
    private BigDecimal riskControlScore;

    @Schema(description = "稳定性评分")
    private BigDecimal stabilityScore;

    @Schema(description = "择时能力评分")
    private BigDecimal timingScore;

    @Schema(description = "经理描述")
    private String managerDesc;

    @Schema(description = "持仓集中度分析")
    private String concentrationRateAnalyse;

    @Schema(description = "换手率分析")
    private String turnoverRateAnalyse;

    @Schema(description = "能力圈与路径依赖分析")
    private String capabilityPathAnalysis;

    @Schema(description = "规模驾驭能力分析")
    private String scaleAbilityAnalysis;

    @Schema(description = "从业背景")
    private String professionalBackground;

    @Schema(description = "产品管理情况分析")
    private String productManagementAnalysis;

    @Schema(description = "稳定性分析")
    private String stabilityAnalysis;

    @Schema(description = "个人持有情况")
    private String personalHolding;

    @Schema(description = "持仓集中度（1-极低，2-低，3-中，4-高，5-极高）")
    private Integer holdingsConcentration;

    @Schema(description = "换手率（1-极低，2-低，3-中，4-高，5-极高）")
    private Integer turnoverRate;

    @Schema(description = "能力路径匹配度（1-极低，2-低，3-中，4-高，5-极高）")
    private Integer backgroundMatch;

    @Schema(description = "规模驾驭能力（1-极低，2-低，3-中，4-高，5-极高）")
    private Integer scaleCapability;

    @Schema(description = "学历（1-全国前三，2-前十名校，3-985，4-211，5-其他）")
    private Integer education;

    @Schema(description = "本基金精力集中度（1-极低，2-低，3-中，4-高，5-极高）")
    private Integer focusOnThisFund;

    @Schema(description = "经理是否持有（1-是，0-否）")
    private Integer isManagerHolding;

    @Schema(description = "获奖记录（1-金牛奖，2-金基金奖，3-明星基金奖，4-晨星奖，5-英华奖，6-其他，7-无）")
    private Integer awardRecords;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建人")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "更新人")
    private String updateBy;
}