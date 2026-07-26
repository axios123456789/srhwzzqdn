package com.xk.srhwzzqdn.model.entity.trialExecutionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "交易预测模拟实体类")
public class PredictionSimulate {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "股票名称")
    private String stockName;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "涨跌预测：1-涨 2-跌")
    private Integer riseFallPrediction;

    @Schema(description = "预测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date predictionTime;

    @Schema(description = "预测内容")
    private String predictionContent;

    @Schema(description = "预测依据")
    private String predictionBasis;

    @Schema(description = "依据类型：1-技术分析 2-基本面分析 3-消息面分析 4-逻辑分析 5-情绪分析，多选逗号分隔")
    private String basisType;

    @Schema(description = "涨跌结果：1-涨 2-跌")
    private Integer riseFallResult;

    @Schema(description = "实际内容")
    private String actualContent;

    @Schema(description = "结果分析")
    private String resultAnalysis;

    @Schema(description = "预测情况：1-完全一致 2-基本一致 3-稍有偏差 4-仅结果一致 5-偏差较大 6-完全偏离")
    private Integer predictionSituation;

    @Schema(description = "预测结果：1-成功 2-失败")
    private Integer predictionResult;

    @Schema(description = "模拟操作：1-模拟买入 2-模拟卖出")
    private Integer simulateOperation;

    @Schema(description = "交易份额（股）")
    private Integer tradeShare;

    @Schema(description = "当前股价")
    private BigDecimal currentPrice;

    @Schema(description = "手续费")
    private BigDecimal handlingFee;

    @Schema(description = "交易状态：1-成功 2-失败")
    private Integer tradeStatus;

    @Schema(description = "所属人")
    private String owner;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
