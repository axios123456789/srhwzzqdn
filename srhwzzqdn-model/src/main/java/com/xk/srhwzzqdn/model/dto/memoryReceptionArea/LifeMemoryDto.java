package com.xk.srhwzzqdn.model.dto.memoryReceptionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "生活记忆条件查询参数")
public class LifeMemoryDto {
    private String memoryNo;
    private List<Integer> lifeType;
    private String beginTime;
    private String endTime;
    private List<Integer> memorySource;
    private String memoryOwner;
}
