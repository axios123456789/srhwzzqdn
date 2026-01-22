package com.xk.srhwzzqdn.model.dto.memoryReceptionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "工作记忆条件查询参数")
public class WorkMemoryDto {
    private String memoryNo;
    private List<Integer> memorySource;
    private String beginTime;
    private String endTime;
    private String workBusinessType;
    private String workTechType;
    private String memoryOwner;
}
