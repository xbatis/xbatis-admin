package com.xbatis.system.dto;

import com.xbatis.system.enums.MenuType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuForm {

    private Long parentId = 0L;

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @NotNull(message = "菜单类型不能为空")
    private MenuType type;

    private String path;

    private String component;

    private String icon;

    private String permission;

    private String redirect;

    private Integer sort = 0;

    private Integer visible = 1;

    private Integer keepAlive = 0;

    private Integer status = 1;

    private String remark;
}
