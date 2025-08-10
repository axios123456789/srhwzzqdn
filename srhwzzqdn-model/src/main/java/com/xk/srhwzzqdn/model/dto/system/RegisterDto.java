package com.xk.srhwzzqdn.model.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RegisterDto {
    @Schema(description = "账号")
    private String account;

    @Schema(description = "连接密码")
    private String password;

    @Schema(description = "确认连接密码")
    private String password2;

    @Schema(description = "大脑主人名称")
    private String userName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "账号等级")
    private Integer level;

    @Schema(description = "认证密码")
    private String verificationPassword;
}
