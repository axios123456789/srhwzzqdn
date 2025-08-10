package com.xk.srhwzzqdn.model.entity.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "个人信息-校园经历实体")
public class SysPersonSchoolInfo {
    private String id;

    @Schema(description = "学校等级")
    private Integer schoolLevel;

    @Schema(description = "学校名称")
    private String school;

    @Schema(description = "毕业时间")
    private String graduationTime;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "个人信息ID")
    private String personInfoId;
}
