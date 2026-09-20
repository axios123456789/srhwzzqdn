package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 短线精选推荐日宽表实体
 * <p>
 * 每天三类（妖股梯队/低位潜伏/趋势延续）各最多10只，按交易日归属（非交易日归上一个交易日），
 * 重算当天先删当天再插入；历史数据用于复盘训练与算法优化。
 */
@Data
@Schema(description = "短线精选推荐日宽表实体类")
public class ShortTermPickDaily {
    @Schema(description = "主键自增")
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "归属交易日（非交易日归上一个交易日）")
    private Date tradeDate;

    @Schema(description = "推荐版本：intraday盘中 / close收盘后")
    private String pickPhase;

    @Schema(description = "推荐类型：yaogu妖股梯队 / qianfu低位潜伏 / qushi趋势延续（字典t_short_term_pick_type）")
    private String pickType;

    @Schema(description = "类内入手优先级 1~10（1为最优先）")
    private Integer priority;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "股票名称")
    private String stockName;

    @Schema(description = "市场：sh沪 / sz深")
    private String market;

    @Schema(description = "所属行业")
    private String industry;

    @Schema(description = "命中概念/题材")
    private String concept;

    @Schema(description = "推荐时点现价（复盘基准价）")
    private BigDecimal price;

    @Schema(description = "推荐时点涨跌幅%")
    private BigDecimal changePct;

    @Schema(description = "总市值(亿)")
    private BigDecimal totalCap;

    @Schema(description = "流通市值(亿)")
    private BigDecimal circCap;

    @Schema(description = "换手率%")
    private BigDecimal turnoverRate;

    @Schema(description = "量比")
    private BigDecimal volumeRatio;

    @Schema(description = "主力净流入(亿)")
    private BigDecimal mainInflow;

    @Schema(description = "主力净流入占成交比%")
    private BigDecimal mainInflowPct;

    @Schema(description = "外盘/内盘比（>1主动买占优）")
    private BigDecimal outerInnerRatio;

    @Schema(description = "连板数（妖股专用）")
    private Integer lianban;

    @Schema(description = "封单额/流通市值%（封板坚决度）")
    private BigDecimal sealRatio;

    @Schema(description = "当日炸板次数")
    private Integer zhabanCount;

    @Schema(description = "首次封板时间(HHMM)")
    private String ztTimeFirst;

    @Schema(description = "60日内涨停次数（涨停基因）")
    private Integer ztCount60d;

    @Schema(description = "低位深度：现价距60日最低点涨幅%")
    private BigDecimal lowDepth;

    @Schema(description = "缩量比：5日均量/20日均量")
    private BigDecimal volShrink;

    @Schema(description = "均线多头排列(MA5>MA10>MA20) 1是/0否")
    private Integer trendMaOk;

    @Schema(description = "近5日累计涨幅%")
    private BigDecimal gain5d;

    @Schema(description = "近20日累计涨幅%")
    private BigDecimal gain20d;

    @Schema(description = "短线选股总分")
    private BigDecimal score;

    @Schema(description = "行为判定：pull_up继续拉升/absorb吸筹/organize强势整理/distribute出货嫌疑/unknown无法判定（字典t_short_term_behavior）")
    private String behavior;

    @Schema(description = "行为判定依据（封板质量/内外盘/资金方向事实）")
    private String behaviorEvidence;

    @Schema(description = "入手优先级理由")
    private String priorityReason;

    @Schema(description = "建议确认介入信号")
    private String confirmSignal;

    @Schema(description = "风险提示")
    private String riskNote;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "入库时间")
    private Date createdAt;
}
