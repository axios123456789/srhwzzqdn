package com.xk.srhwzzqdn.model.entity.system;

import com.xk.srhwzzqdn.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统用户实体类")
public class SysUser extends BaseEntity {
    @Schema(description = "账号")
    private String account;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "状态 1正常 0禁用")
    private Integer status;

    @Schema(description = "账号等级：1开发者账号 2管理员账号 3普通账号")
    private Integer level;

    private String levelName;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户描述")
    private String description;
}
