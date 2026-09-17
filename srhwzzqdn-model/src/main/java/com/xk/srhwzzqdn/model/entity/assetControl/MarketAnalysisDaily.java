package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 市场实时分析每日宽表实体
 * <p>
 * 按天存储（market_date 唯一）：每次实时分析若当天已有记录则先删当天数据再重新入库，
 * 保证库中始终是该交易日最新一次的市场分析快照；绝不触碰其他交易日数据。
 */
@Data
@Schema(description = "市场实时分析每日宽表实体类")
public class MarketAnalysisDaily {
    @Schema(description = "主键自增")
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "交易日（按天唯一）")
    private Date marketDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "本次实时分析的采集时间")
    private Date analysisTime;

    @Schema(description = "数据所处阶段：盘前/竞价/早盘/盘中/尾盘/已收盘/非交易时段")
    private String marketPhase;

    @Schema(description = "市场在做什么（一句话结论）")
    private String marketStatus;

    @Schema(description = "市场情绪温度0-100")
    private Integer sentimentScore;

    @Schema(description = "情绪档位：冰点/低迷/中性/活跃/亢奋")
    private String sentimentLevel;

    @Schema(description = "上证指数点位")
    private BigDecimal shClose;

    @Schema(description = "上证涨跌幅%")
    private BigDecimal shChangePct;

    @Schema(description = "深证成指点位")
    private BigDecimal szClose;

    @Schema(description = "深证成指涨跌幅%")
    private BigDecimal szChangePct;

    @Schema(description = "创业板指点位")
    private BigDecimal cybClose;

    @Schema(description = "创业板指涨跌幅%")
    private BigDecimal cybChangePct;

    @Schema(description = "科创50点位")
    private BigDecimal kc50Close;

    @Schema(description = "科创50涨跌幅%")
    private BigDecimal kc50ChangePct;

    @Schema(description = "两市实时成交额(元)")
    private BigDecimal totalAmount;

    @Schema(description = "昨日全天两市成交额(元)")
    private BigDecimal prevTotalAmount;

    @Schema(description = "今日实时成交额/昨日全天成交额")
    private BigDecimal amountRatio;

    @Schema(description = "全市场上涨家数")
    private Integer upCount;

    @Schema(description = "全市场下跌家数")
    private Integer downCount;

    @Schema(description = "全市场平盘家数")
    private Integer flatCount;

    @Schema(description = "涨停家数")
    private Integer limitUpCount;

    @Schema(description = "跌停家数")
    private Integer limitDownCount;

    @Schema(description = "炸板家数")
    private Integer zhaBanCount;

    @Schema(description = "炸板率%（炸板/涨停+炸板）")
    private BigDecimal zhaBanRate;

    @Schema(description = "最高连板数")
    private Integer maxLianban;

    @Schema(description = "连板梯队JSON")
    private String lianbanStocks;

    @Schema(description = "全市场主力净流入(元)")
    private BigDecimal mainNetInflow;

    @Schema(description = "超大单净流入(元)")
    private BigDecimal ultraNetInflow;

    @Schema(description = "大单净流入(元)")
    private BigDecimal bigNetInflow;

    @Schema(description = "中单净流入(元)")
    private BigDecimal midNetInflow;

    @Schema(description = "小单净流入(元)")
    private BigDecimal smallNetInflow;

    @Schema(description = "两融余额(元,T-1)")
    private BigDecimal marginBalance;

    @Schema(description = "融资净买入(元,T-1)")
    private BigDecimal marginNetBuy;

    @Schema(description = "主拉板块TOP JSON")
    private String hotSectors;

    @Schema(description = "主弃板块 JSON")
    private String coldSectors;

    @Schema(description = "市场主线（规则初判）")
    private String mainLine;

    @Schema(description = "主线持续性规则初判：一日游/波段/主线延续")
    private String mainLineSustainability;

    @Schema(description = "主线龙头股JSON（含新手可买标记）")
    private String leaders;

    @Schema(description = "核心消息面JSON")
    private String newsHighlights;

    @Schema(description = "AI：市场在做什么/情绪综合")
    private String aiMarketStatus;

    @Schema(description = "AI：核心消息面与驱动逻辑")
    private String aiLogic;

    @Schema(description = "AI：资金流向与增量退场判断")
    private String aiMoneyFlow;

    @Schema(description = "AI：主线板块与持续性（一日游vs长期）判断")
    private String aiMainline;

    @Schema(description = "AI：明日展望")
    private String aiOutlook;

    @Schema(description = "AI：历史分析准确性复盘（昨日展望vs今日实际）")
    private String aiHistoryReview;

    @Schema(description = "实时快照全量JSON（供历史对比AI复盘使用）")
    private String realtimeJson;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private Date updateTime;
}
