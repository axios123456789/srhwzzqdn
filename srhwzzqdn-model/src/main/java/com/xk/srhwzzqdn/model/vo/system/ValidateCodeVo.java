package com.xk.srhwzzqdn.model.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ValidateCodeVo {
    @Schema(description = "验证码key")
    private String codeKey;

    @Schema(description = "验证码value")
    private String codeValue;
}
