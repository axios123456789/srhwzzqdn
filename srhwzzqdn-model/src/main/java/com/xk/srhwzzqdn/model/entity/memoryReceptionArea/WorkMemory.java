package com.xk.srhwzzqdn.model.entity.memoryReceptionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "工作记忆表")
public class WorkMemory {
    @Schema(description = "主键")
    private String id;

    @Schema(description = "记忆编号")
    private String memoryNo;

    @Schema(description = "工作业务类型")
    private String workBusinessType;

    @Schema(description = "工作技术类型")
    private String workTechType;

    @Schema(description = "工作记忆来源")
    private Integer memorySource;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "工作时长")
    private Double workDuration;

    @Schema(description = "工作内容")
    private String workContent;

    @Schema(description = "工作业务笔记")
    private String workBusinessNode;

    @Schema(description = "工作技术笔记")
    private String workTechNode;

    @Schema(description = "工作文档")
    private String workDocument;

    @Schema(description = "记忆所属人（账号id）")
    private String memoryOwner;

    @Schema(description = "记忆所属人（名称）")
    private String memoryOwnerName;

    @Schema(description = "工作地点")
    private String workAddress;

    @Schema(description = "具体工作地点")
    private String workAddressDetail;

//    @Schema(description = "记录人")
//    private String recordBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "工作记忆图片")
    private String memoryImages;
}
