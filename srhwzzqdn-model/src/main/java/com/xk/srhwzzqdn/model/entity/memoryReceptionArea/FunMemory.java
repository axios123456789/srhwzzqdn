package com.xk.srhwzzqdn.model.entity.memoryReceptionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "娱乐记忆实体类")
public class FunMemory {
    private Integer id;

    @Schema(description = "记忆编号")
    private String memoryNo;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "娱乐时长")
    private BigDecimal funDuration;

    @Schema(description = "娱乐记忆类型")
    private Integer funType;

    @Schema(description = "娱乐软件")
    private Integer funApp;

    @Schema(description = "娱乐消费")
    private BigDecimal funConsume;

    @Schema(description = "娱乐记忆内容")
    private String funContent;

    @Schema(description = "娱乐记忆所属人")
    private String memoryOwner;

    private String memoryOwnerName;

    @Schema(description = "娱乐记忆地点")
    private String memoryPlace;

    @Schema(description = "娱乐记忆地点详细")
    private String memoryPlaceDetail;

    @Schema(description = "娱乐记忆册")
    private String memoryImages;

    @Schema(description = "娱乐记忆来源")
    private Integer memorySource;

    @Schema(description = "原始记忆编号")
    private String rowMemoryNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "修改人")
    private String updateBy;
}
