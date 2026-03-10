package com.xk.srhwzzqdn.model.dto.memoryReceptionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "娱乐记忆条件查询参数")
public class FunMemoryDto {
    private String memoryNo;
    private String beginTime;
    private String endTime;
    private List<Integer> memorySource;
    private List<Integer> funType;
    private List<Integer> funApp;
    private String memoryOwner;
}
