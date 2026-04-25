package com.xk.srhwzzqdn.model.entity.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统通用配置表")
public class SysCommConfig {
    @Schema(description = "用于获取配置值的唯一id")
    private String id;

    @Schema(description = "配置值")
    private String value;

    @Schema(description = "配置描述")
    private String description;
}
