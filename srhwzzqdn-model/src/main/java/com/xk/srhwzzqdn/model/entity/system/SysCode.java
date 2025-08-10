package com.xk.srhwzzqdn.model.entity.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统码值表（针对后面出现的有多级联级码值情况）")
public class SysCode {
    private String id;
    private String value;
    private String text;
    private Integer sortValue;
    private String parentId;
    private Integer status;
    private String type;
}
