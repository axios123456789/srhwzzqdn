package com.xk.srhwzzqdn.model.dto.memoryReceptionArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "原始记忆条件查询参数")
public class RowMemoryDto {
    private String contact;
    private List<Integer> contactType;
    private String recordBeginTime;
    private String recordEndTime;
    private List<Integer> rowMemoryType;
    private List<Integer> memorySource;
    private List<Integer> memoryAssociativeStatus;
    private String memoryNo;
    private String memoryOwner;
}
