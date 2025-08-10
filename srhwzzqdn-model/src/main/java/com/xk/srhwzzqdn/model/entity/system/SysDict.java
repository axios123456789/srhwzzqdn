package com.xk.srhwzzqdn.model.entity.system;

import com.xk.srhwzzqdn.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "数据字典实体类（码表）")
public class SysDict extends BaseEntity {
    @Schema(description = "字典码值")
    private Integer code;

    @Schema(description = "字典值")
    private String value;

    @Schema(description = "字典类型")
    private String type;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态 1正常 0禁用")
    private Integer status;
}
