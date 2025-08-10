package com.xk.srhwzzqdn.model.entity.system;

import com.xk.srhwzzqdn.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "系统菜单实体类")
public class SysMenu extends BaseEntity {
    @Schema(description = "父级id")
    private String parentId;

    @Schema(description = "菜单标题")
    private String title;

    @Schema(description = "组件名称")
    private String component;

    @Schema(description = "排序")
    private Integer sortValue;

    @Schema(description = "状态 1 正常 0 禁用")
    private Integer status;

    //下级列表
    @Schema(description = "子节点")
    private List<SysMenu> children;
}
