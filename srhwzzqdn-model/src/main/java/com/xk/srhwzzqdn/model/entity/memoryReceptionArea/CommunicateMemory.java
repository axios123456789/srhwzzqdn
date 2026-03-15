package com.xk.srhwzzqdn.model.entity.memoryReceptionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: xk
 * @description: 交际记忆实体类
 */
@Data
@Schema(description = "交际记忆实体类")
public class CommunicateMemory {

    @Schema(description = "主键自增")
    private Integer id;

    @Schema(description = "记忆编号")
    private String memoryNo;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "交际时长")
    private BigDecimal communicateDuration;

    @Schema(description = "记忆所属人")
    private String memoryOwner;

    @Schema(description = "记忆所属人姓名")
    private String memoryOwnerName;

    @Schema(description = "记忆地点")
    private String memoryPlace;

    @Schema(description = "记忆地点详细")
    private String memoryPlaceDetail;

    @Schema(description = "交际记录册")
    private String memoryImages;

    @Schema(description = "交际记忆来源")
    private Integer memorySource;

    @Schema(description = "原始记忆编号")
    private String rowMemoryNo;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "交际人关系")
    private Integer communicatorRelation;

    @Schema(description = "交际人名称")
    private String communicator;

    @Schema(description = "交际类型（工作，交易，闲聊，问候）")
    private String communicateType;

    @Schema(description = "交际方式（线下，微信，QQ，抖音）")
    private Integer communicateWay;

    @Schema(description = "交际内容")
    private String communicateContent;
}
