package com.xk.srhwzzqdn.model.entity.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "角色菜单关联关系")
public class SysRoleMenu {
    private String id;

    private String roleId;

    private String menuId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "是否半开 1 是 0 否")
    private Integer isHalf;

}
