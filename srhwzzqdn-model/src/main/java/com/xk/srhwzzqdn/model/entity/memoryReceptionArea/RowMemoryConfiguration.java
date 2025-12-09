package com.xk.srhwzzqdn.model.entity.memoryReceptionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "原始记忆配置表")
public class RowMemoryConfiguration {
    private String id;

    @Schema(description = "时间阶段类型")
    private Integer timePeriodType;

    @Schema(description = "时间阶段类型描述")
    private String timePeriodDescription;

    @Schema(description = "开始时间阶段")
    private Double beginTimePeriod;

    @Schema(description = "结束时间阶段")
    private Double endTimePeriod;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "字段名称排序")
    private Integer fieldNameSort;

    @Schema(description = "字段值")
    private String fieldValue;

    @Schema(description = "字段值出现次数")
    private Integer fieldValueCount;

    @Schema(description = "配置记忆所属人")
    private String configurationMemoryOwner;
}
