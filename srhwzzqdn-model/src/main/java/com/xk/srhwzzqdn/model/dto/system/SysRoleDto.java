package com.xk.srhwzzqdn.model.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色请求参数实体类")
public class SysRoleDto {
    @Schema(description = "角色名称")
    private String roleName;
}
