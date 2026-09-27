package com.xk.srhwzzqdn.model.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "接口配置查询DTO")
public class SysCommConfigQueryDto {
    @Schema(description = "接口名称(模糊)")
    private String interfaceName;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "id(模糊)")
    private String id;
}