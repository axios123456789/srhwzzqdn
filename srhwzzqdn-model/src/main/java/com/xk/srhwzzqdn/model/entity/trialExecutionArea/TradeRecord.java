package com.xk.srhwzzqdn.model.entity.trialExecutionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "交易记录复盘实体类")
public class TradeRecord {
    @Schema(description = "主键")
    private Long id;

    // ==================== 组1：交易基本信息 ====================
    @Schema(description = "交易时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date tradeDatetime;

    @Schema(description = "股票名称")
    private String stockName;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "买卖方向（字典t_trial_trade_direction的code）")
    private Integer tradeDirection;

    @Schema(description = "成交价格")
    private BigDecimal tradePrice;

    @Schema(description = "成交数量（股）")
    private Integer tradeQuantity;

    @Schema(description = "成交金额（元）")
    private BigDecimal tradeAmount;

    @Schema(description = "交易时段（字典t_trial_trade_time_slot的code）")
    private Integer timeSlot;

    // ==================== 组2：交易背景 ====================
    @Schema(description = "当时大盘状态（字典t_trial_review_market_status的code）")
    private Integer marketStatus;

    @Schema(description = "个股位置（字典t_trial_trade_position的code）")
    private Integer stockPosition;

    @Schema(description = "当时个股涨跌幅（%）")
    private BigDecimal stockChangePct;

    // ==================== 组3：心理状态 ====================
    @Schema(description = "交易时心理状态（字典t_trial_trade_psychology的code）")
    private Integer psychology;

    @Schema(description = "情绪强度（1-5）")
    private Integer emotionIntensity;

    @Schema(description = "是否符合计划（字典t_trial_trade_plan_match的code）")
    private Integer followPlan;

    // ==================== 组4：交易逻辑 ====================
    @Schema(description = "为何交易（买入/卖出逻辑）")
    private String tradeReason;

    @Schema(description = "预期收益（%）")
    private BigDecimal expectedProfitPct;

    @Schema(description = "止损价")
    private BigDecimal stopLossPrice;

    @Schema(description = "止盈价")
    private BigDecimal takeProfitPrice;

    // ==================== 组5：结果与反思 ====================
    @Schema(description = "本笔交易盈亏（%）")
    private BigDecimal profitPct;

    @Schema(description = "持仓时长（分钟）")
    private Integer holdingDuration;

    @Schema(description = "执行评分（字典t_trial_trade_execute_rating的code）")
    private Integer executeRating;

    @Schema(description = "反思总结")
    private String reflection;

    @Schema(description = "教训")
    private String lesson;

    // ==================== 关联与通用 ====================
    @Schema(description = "关联复盘日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date reviewDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "所属人")
    private String owner;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}