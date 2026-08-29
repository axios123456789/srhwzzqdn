package com.xk.srhwzzqdn.model.dto.trialExecutionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "交易预测模拟条件查询参数")
public class PredictionSimulateDto {
    @Schema(description = "股票名称")
    private String stockName;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "涨跌预测")
    private List<Integer> riseFallPrediction;

    @Schema(description = "依据类型（多选查询，使用FIND_IN_SET匹配）")
    private List<String> basisType;


    @Schema(description = "预测时间（起）")
    private String predictionTimeStart;

    @Schema(description = "预测时间（止）")
    private String predictionTimeEnd;

    @Schema(description = "预测情况")
    private List<Integer> predictionSituation;

    @Schema(description = "预测结果")
    private List<Integer> predictionResult;

    @Schema(description = "模拟操作")
    private List<Integer> simulateOperation;

    @Schema(description = "交易状态")
    private List<Integer> tradeStatus;

    @Schema(description = "所属人")
    private String owner;
}
