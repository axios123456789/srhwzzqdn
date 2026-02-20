package com.xk.srhwzzqdn.model.entity.memoryReceptionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "生活记忆实体类")
public class lifeMemory {
    private Integer id;

    @Schema(description = "记忆编号")
    private String memoryNo;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "生活记忆类型")
    private Integer lifeType;

    @Schema(description = "生活记忆内容")
    private String lifeContent;

    @Schema(description = "生活记忆消费")
    private Double lifeConsume;

    @Schema(description = "生活记忆地点")
    private String memoryPlace;

    @Schema(description = "生活记忆地点详细")
    private String memoryPlaceDetail;

    @Schema(description = "生活记忆册")
    private String memoryImage;

    @Schema(description = "生活记忆来源")
    private Integer memorySource;

    @Schema(description = "生活记忆所属人")
    private String memoryOwner;

    @Schema(description = "原始记忆编号")
    private String rowMemoryNo;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "修改人")
    private String updateBy;
}
