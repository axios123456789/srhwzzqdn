package com.xk.srhwzzqdn.model.entity.trialExecutionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "每日复盘实体类")
public class DailyReview {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "复盘日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date reviewDate;

    // ==================== 组1：大盘环境 ====================
    @Schema(description = "上证涨跌幅（%）")
    private BigDecimal shChangePct;

    @Schema(description = "深证涨跌幅（%）")
    private BigDecimal szChangePct;

    @Schema(description = "创业板涨跌幅（%）")
    private BigDecimal cybChangePct;

    @Schema(description = "两市成交额（亿元）")
    private BigDecimal totalAmount;

    @Schema(description = "上涨家数")
    private Integer riseCount;

    @Schema(description = "下跌家数")
    private Integer fallCount;

    @Schema(description = "市场状态（字典t_trial_review_market_status的code）")
    private Integer marketStatus;

    // ==================== 组2：情绪指标 ====================
    @Schema(description = "涨停家数（剔除ST）")
    private Integer limitUpCount;

    @Schema(description = "跌停家数（剔除ST）")
    private Integer limitDownCount;

    @Schema(description = "连板家数（2连板及以上）")
    private Integer continuousBoardCount;

    @Schema(description = "昨日涨停溢价（%）")
    private BigDecimal yesterdayPremiumPct;

    @Schema(description = "情绪温度（字典t_trial_review_emotion_temp的code）")
    private Integer emotionTemp;

    @Schema(description = "北向资金净流入（万元）")
    private BigDecimal northFlowAmount;

    @Schema(description = "炸板家数")
    private Integer brokenBoardCount;

    @Schema(description = "炸板率（%）")
    private BigDecimal brokenBoardRate;

    // ==================== 组3：主线与龙头 ====================
    @Schema(description = "主线板块1（字典t_trial_review_sector的code）")
    private Integer mainSector1;

    @Schema(description = "主线板块2（字典t_trial_review_sector的code）")
    private Integer mainSector2;

    @Schema(description = "主线板块3（字典t_trial_review_sector的code）")
    private Integer mainSector3;

    @Schema(description = "板块涨停家数")
    private Integer sectorLimitUpCount;

    @Schema(description = "龙头股名称")
    private String leaderStockName;

    @Schema(description = "龙头股代码")
    private String leaderStockCode;

    @Schema(description = "龙头涨停时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaderLimitUpTime;

    @Schema(description = "龙头封单金额（万元）")
    private BigDecimal leaderSealAmount;

    @Schema(description = "龙头连板数（几连板）")
    private Integer leaderContinuousBoard;

    // ==================== 组4：次日策略 ====================
    @Schema(description = "适配体系（字典t_trial_review_adapt_system的code）")
    private Integer adaptSystem;

    @Schema(description = "计划仓位上限（%）")
    private BigDecimal planPositionLimit;

    @Schema(description = "关注标的（多个逗号分隔）")
    private String watchTargets;

    @Schema(description = "买入条件")
    private String buyCondition;

    @Schema(description = "风险预警")
    private String riskWarning;

    @Schema(description = "止损线（%）")
    private BigDecimal stopLossPct;

    @Schema(description = "止盈线（%）")
    private BigDecimal takeProfitPct;

    // ==================== 组5：持仓与自评 ====================
    @Schema(description = "今日操作记录")
    private String todayOperation;

    @Schema(description = "操作自评（字典t_trial_review_self_rating的code）")
    private Integer operationSelfRating;

    @Schema(description = "当日盈亏（%）")
    private BigDecimal dailyProfitPct;

    @Schema(description = "持仓盈亏（%）")
    private BigDecimal positionProfitPct;

    @Schema(description = "今日交易次数")
    private Integer tradeCount;

    @Schema(description = "盈利交易次数")
    private Integer winTradeCount;

    // ==================== 组6：总结反思 ====================
    @Schema(description = "经验总结")
    private String experience;

    @Schema(description = "教训反思")
    private String lesson;

    @Schema(description = "改进点")
    private String improvePoint;

    @Schema(description = "明日关注重点")
    private String tomorrowFocus;

    // ==================== 通用字段 ====================
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