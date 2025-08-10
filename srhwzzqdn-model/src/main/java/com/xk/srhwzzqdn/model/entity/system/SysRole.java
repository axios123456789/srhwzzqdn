package com.xk.srhwzzqdn.model.entity.system;

import com.xk.srhwzzqdn.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统角色实体类")
public class SysRole extends BaseEntity {
    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色码")
    private String roleCode;

    @Schema(description = "描述")
    private String description;
}
