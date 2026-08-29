package com.xk.srhwzzqdn.model.vo.trialExecutionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "预测统计报表VO")
public class PredictionReportVo {
    @Schema(description = "总预测次数")
    private Integer totalCount;

    @Schema(description = "预测成功次数")
    private Integer successCount;

    @Schema(description = "预测失败次数")
    private Integer failCount;

    @Schema(description = "预测准确率")
    private BigDecimal accuracyRate;

    @Schema(description = "按依据类型统计")
    private List<BasisTypeStat> basisTypeStats;

    @Schema(description = "按预测源统计")
    private List<PredictionSourceStat> predictionSourceStats;

    @Schema(description = "按预测情况统计")
    private List<SituationStat> situationStats;

    @Schema(description = "按月统计趋势")
    private List<MonthlyTrend> monthlyTrends;

    @Schema(description = "模拟操作统计")
    private SimulateTradeStat simulateTradeStat;

    @Schema(description = "按股票统计")
    private List<StockStat> stockStats;

    @Data
    @Schema(description = "依据类型统计")
    public static class BasisTypeStat {
        @Schema(description = "依据类型（逗号分隔字符串）")
        private String basisType;
        @Schema(description = "总次数")
        private Integer count;
        @Schema(description = "成功次数")
        private Integer successCount;
        @Schema(description = "成功率")
        private BigDecimal successRate;
    }

    @Data
    @Schema(description = "预测源统计")
    public static class PredictionSourceStat {
        @Schema(description = "预测源：1-人工预测 2-智能预测")
        private Integer predictionSource;
        @Schema(description = "总次数")
        private Integer count;
        @Schema(description = "成功次数")
        private Integer successCount;
        @Schema(description = "成功率")
        private BigDecimal successRate;
    }

    @Data
    @Schema(description = "预测情况统计")
    public static class SituationStat {
        @Schema(description = "预测情况")
        private Integer predictionSituation;
        @Schema(description = "次数")
        private Integer count;
        @Schema(description = "占比")
        private BigDecimal percentage;
    }

    @Data
    @Schema(description = "月度趋势统计")
    public static class MonthlyTrend {
        @Schema(description = "月份")
        private String month;
        @Schema(description = "总次数")
        private Integer totalCount;
        @Schema(description = "成功次数")
        private Integer successCount;
        @Schema(description = "成功率")
        private BigDecimal successRate;
    }

    @Data
    @Schema(description = "模拟操作统计")
    public static class SimulateTradeStat {
        @Schema(description = "买入总次数")
        private Integer totalBuyCount;
        @Schema(description = "卖出总次数")
        private Integer totalSellCount;
        @Schema(description = "买入总金额")
        private BigDecimal totalBuyAmount;
        @Schema(description = "卖出总金额")
        private BigDecimal totalSellAmount;
        @Schema(description = "总手续费")
        private BigDecimal totalHandlingFee;
        @Schema(description = "模拟盈亏")
        private BigDecimal totalProfitLoss;
    }

    @Data
    @Schema(description = "股票预测统计")
    public static class StockStat {
        @Schema(description = "股票名称")
        private String stockName;
        @Schema(description = "股票代码")
        private String stockCode;
        @Schema(description = "预测次数")
        private Integer predictCount;
        @Schema(description = "成功次数")
        private Integer successCount;
        @Schema(description = "成功率")
        private BigDecimal successRate;
    }
}
