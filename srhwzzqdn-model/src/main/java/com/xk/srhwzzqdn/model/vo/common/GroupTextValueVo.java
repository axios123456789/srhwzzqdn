package com.xk.srhwzzqdn.model.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "通用文本值响应对象")
public class GroupTextValueVo {
    private String text;
    private BigDecimal value;
}
