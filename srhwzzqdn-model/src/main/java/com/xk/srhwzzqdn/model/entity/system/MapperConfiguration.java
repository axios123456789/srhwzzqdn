package com.xk.srhwzzqdn.model.entity.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "映射配置实体类")
public class MapperConfiguration {
    private String id;

    @Schema(description = "映射业务类型")
    private String type;

    @Schema(description = "业务类型描述")
    private String typeDescription;

    @Schema(description = "映射A类型字段1")
    private Integer mapperFieldA1;

    @Schema(description = "映射A类型字段2")
    private String mapperFieldA2;

    @Schema(description = "映射B类型字段1")
    private String mapperFieldB1;

    @Schema(description = "映射B类型字段2")
    private Integer mapperFieldB2;

    @Schema(description = "映射规则描述")
    private String mapperRuleDescription;

    @Schema(description = "映射B类型字段3")
    private String mapperFieldB3;
}
