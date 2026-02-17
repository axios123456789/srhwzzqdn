package com.xk.srhwzzqdn.model.entity.memoryReceptionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "记忆联想转换数据类")
public class AssociativeMemory {
    @Schema(description = "原始记忆id")
    private String row_id;

    @Schema(description = "联想开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date begin_time;

    @Schema(description = "联想结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date end_time;

    @Schema(description = "联想关系人类型")
    private Integer contactType; //联想关系人类型

    @Schema(description = "联想关系人名称")
    private String contact; //联想关系人名称

    @Schema(description = "联想记忆地点")
    private String memoryPlace; //联想记忆地点

    @Schema(description = "联想记忆详细地点")
    private String memoryPlaceDetail; //联想记忆地点详情

    @Schema(description = "联想记忆地点简称")
    private Integer memoryPlaceShort; //联想地点简称

    @Schema(description = "联想原始记忆类型")
    private Integer rowMemoryType; //联想原始记忆类型

    @Schema(description = "联想原始记忆内容")
    private String rowMemoryContent; //联想原始记忆内容

    @Schema(description = "联想原始记忆原因")
    private String rowMemoryReason; //联想记忆原因

    @Schema(description = "联想原始记忆行为")
    private String rowMemoryAction; //联想记忆行为

    @Schema(description = "联想记忆所属人")
    private String memoryOwner; //联想记忆人

    @Schema(description = "联想记忆来源")
    private Integer memorySource; //联想记忆来源

    @Schema(description = "联想记忆状态")
    private String memoryImages; //联想图片

    @Schema(description = "联想记忆编号")
    private String memoryNo; //联想记忆编号

    @Schema(description = "联想文件")
    private String document; //联想文件

    @Schema(description = "工作业务类型")
    private String workBusinessType;

    @Schema(description = "工作技术类型")
    private String workTechType; //工作技术类型

    @Schema(description = "工作业务笔记")
    private String workBusinessNode;

    @Schema(description = "工作技术笔记")
    private String workTechNode; //工作技术笔记
}
