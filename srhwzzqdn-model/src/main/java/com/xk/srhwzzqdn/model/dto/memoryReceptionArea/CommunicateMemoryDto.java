package com.xk.srhwzzqdn.model.dto.memoryReceptionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author: xk
 * @description: 交际记忆条件查询参数
 */
@Data
@Schema(description = "交际记忆条件查询参数")
public class CommunicateMemoryDto {

    @Schema(description = "记忆编号")
    private String memoryNo;

    @Schema(description = "交际人名称")
    private String communicator;

    @Schema(description = "开始时间")
    private String beginTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "交际人关系")
    private List<Integer> communicatorRelation;

    @Schema(description = "交际类型")
    private List<String> communicateType;

    @Schema(description = "交际方式")
    private List<Integer> communicateWay;

    @Schema(description = "交际记忆来源")
    private List<Integer> memorySource;

    @Schema(description = "记忆所属人")
    private String memoryOwner;
}
