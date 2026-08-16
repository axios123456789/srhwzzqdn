package com.xk.srhwzzqdn.model.dto.trialExecutionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "交易记录查询参数")
public class TradeRecordDto {
    @Schema(description = "股票名称")
    private String stockName;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "买卖方向（多选）")
    private List<Integer> tradeDirection;

    @Schema(description = "心理状态（多选）")
    private List<Integer> psychology;

    @Schema(description = "交易时间开始")
    private String tradeTimeStart;

    @Schema(description = "交易时间结束")
    private String tradeTimeEnd;

    @Schema(description = "是否符合计划（多选）")
    private List<Integer> followPlan;

    @Schema(description = "执行评分（多选）")
    private List<Integer> executeRating;

    @Schema(description = "所属人")
    private String owner;
}