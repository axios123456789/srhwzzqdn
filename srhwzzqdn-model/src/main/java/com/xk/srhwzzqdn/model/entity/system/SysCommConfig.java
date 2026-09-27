package com.xk.srhwzzqdn.model.entity.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "系统通用配置表")
public class SysCommConfig {
    @Schema(description = "用于获取配置值的唯一id")
    private String id;

    @Schema(description = "配置值")
    private String value;

    @Schema(description = "配置描述")
    private String description;

    @Schema(description = "接口名称")
    private String interfaceName;

    @Schema(description = "兜底URL(原写死URL)")
    private String fallbackUrl;

    @Schema(description = "分类: stock/trial/fund/common")
    private String category;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "创建者")
    private String createBy;

    @Schema(description = "修改者")
    private String updateBy;
}
