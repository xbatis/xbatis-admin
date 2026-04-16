package com.xbatis.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoleForm {

    @NotBlank(message = "角色名称不能为空")
    private String name;

    @NotNull(message = "状态不能为空")
    private Integer enabled;

    private Integer sort = 0;

    private Integer deletable = 1;

    private String remark;

    private List<Long> menuIds;
}
