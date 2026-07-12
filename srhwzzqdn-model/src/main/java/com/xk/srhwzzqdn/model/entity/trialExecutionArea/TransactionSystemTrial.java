package com.xk.srhwzzqdn.model.entity.trialExecutionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "交易系统试验实体类")
public class TransactionSystemTrial {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "交易类型：1-股票买入 2-股票卖出")
    private Integer tradeType;

    @Schema(description = "交易对象名称")
    private String targetName;

    @Schema(description = "计划类型：1-止盈 2-止损 3-建仓")
    private Integer planType;

    @Schema(description = "交易计划内容")
    private String planContent;

    @Schema(description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date planStartTime;

    @Schema(description = "计划结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date planEndTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "当前价")
    private BigDecimal currentPrice;

    @Schema(description = "计划交易价")
    private BigDecimal planPrice;

    @Schema(description = "开盘价")
    private BigDecimal openPrice;

    @Schema(description = "实际成交价")
    private BigDecimal actualPrice;

    @Schema(description = "收盘价")
    private BigDecimal closePrice;

    @Schema(description = "交易状态：1-未开始 2-进行中 3-已结束")
    private Integer tradeStatus;

    @Schema(description = "遵守规则id")
    private String complyRuleIds;

    @Schema(description = "违反规则id")
    private String violateRuleIds;

    @Schema(description = "交易结果：1-成功 0-失败")
    private Integer tradeResult;

    @Schema(description = "交易失败类型：1-追高 2-杀跌 3-抄底")
    private Integer tradeFailType;

    @Schema(description = "结果复盘")
    private String resultReview;

    @Schema(description = "所属人")
    private String owner;

    @Schema(description = "是否触发计划：1-是 0-否")
    private Integer isUsePlan;
}