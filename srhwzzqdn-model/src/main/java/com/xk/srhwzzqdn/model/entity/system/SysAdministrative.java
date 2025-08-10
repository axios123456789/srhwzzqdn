package com.xk.srhwzzqdn.model.entity.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "中国行政区划码表（仅到县级）")
public class SysAdministrative {
    @Schema(description = "码值")
    private String value;

    @Schema(description = "简称")
    private String shortLabel;

    @Schema(description = "名称")
    private String label;

    @Schema(description = "英文名称")
    private String pinyin;

    @Schema(description = "级别")
    private Integer rank;

    @Schema(description = "父级码值")
    private String parentValue;

    private String id;

    private List<SysAdministrative> children;
}
