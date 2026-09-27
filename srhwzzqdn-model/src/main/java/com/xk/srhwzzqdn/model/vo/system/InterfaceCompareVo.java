package com.xk.srhwzzqdn.model.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "接口新旧URL比对VO")
public class InterfaceCompareVo {
    @Schema(description = "配置id")
    private String id;
    @Schema(description = "接口名称")
    private String interfaceName;
    @Schema(description = "旧URL")
    private String oldUrl;
    @Schema(description = "AI获取的新URL")
    private String newUrl;
    @Schema(description = "是否变化")
    private Boolean changed;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "新URL是否通过可用性验证")
    private Boolean validated;
    @Schema(description = "验证信息")
    private String validateMsg;
}