package com.xk.srhwzzqdn.model.entity.memoryReceptionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "原始记忆表")
public class RowMemory {
    private String id;

    @Schema(description = "记录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recordTime;

    @Schema(description = "关系人类型（亲人，朋友...）")
    private Integer contactType;

    @Schema(description = "关系人名称")
    private String contact;

    @Schema(description = "记忆地点")
    private String memoryPlace;

    @Schema(description = "记忆详细地点")
    private String memoryPlaceDetail;

    @Schema(description = "记忆地点简称（如1：家 2：学校）")
    private Integer memoryPlaceShort;

    @Schema(description = "原始记忆类型（1：工作记忆 2：生活记忆 3：娱乐记忆 4：交际记忆 5：学习记忆）")
    private Integer rowMemoryType;

    @Schema(description = "记忆内容")
    private String rowMemoryContent;

    @Schema(description = "原始记忆原因")
    private String rowMemoryReason;

    @Schema(description = "原始记忆行为")
    private String rowMemoryAction;

    @Schema(description = "记忆所属人（记录账号id）")
    private String memoryOwner;

    private String memoryOwnerName;

    @Schema(description = "记忆来源(1:手动录入 2:响应式录入 3:智能录入)")
    private Integer memorySource;

    @Schema(description = "记忆联想状态（1：未联想 2：已联想工作记忆 3：已联想生活记忆：4：已联想娱乐记忆 5：已联系交际记忆 6:已联系学习记忆 7：无需联想）")
    private Integer memoryAssociativeStatus;

    @Schema(description = "记忆册")
    private String memoryImages;

    @Schema(description = "记录结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recordEndTime;

    @Schema(description = "记录人")
    private String recordBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private String updateBy;
}
