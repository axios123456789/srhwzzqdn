package com.xk.srhwzzqdn.model.entity.memoryReceptionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "学习记忆实体类")
public class LearnMemory {
    private Integer id;

    @Schema(description = "记忆编号")
    private String memoryNo;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "学习时长")
    private BigDecimal learnDuration;

    @Schema(description = "学习记忆所属人")
    private String memoryOwner;

    private String memoryOwnerName;

    @Schema(description = "学习记忆地点")
    private String memoryPlace;

    @Schema(description = "学习记忆地点详细")
    private String memoryPlaceDetail;

    @Schema(description = "学习记忆册")
    private String memoryImages;

    @Schema(description = "学习记忆来源")
    private Integer memorySource;

    @Schema(description = "原始记忆编号")
    private String rowMemoryNo;

    @Schema(description = "学习记忆类型")
    private String learnType;

    @Schema(description = "学习内容")
    private String learnContent;

    @Schema(description = "学习笔记")
    private String learnNode;

    @Schema(description = "学习文档")
    private String learnDocument;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "修改人")
    private String updateBy;
}
