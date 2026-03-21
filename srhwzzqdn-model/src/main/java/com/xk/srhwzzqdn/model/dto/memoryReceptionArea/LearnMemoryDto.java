package com.xk.srhwzzqdn.model.dto.memoryReceptionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "学习记忆条件查询参数")
public class LearnMemoryDto {
    private String memoryNo;
    private String beginTime;
    private String endTime;
    private List<Integer> memorySource;
    private String learnType;
    private String memoryOwner;
}
