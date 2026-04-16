package com.xbatis.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserForm {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "显示名不能为空")
    private String displayName;

    private String email;

    private String phone;

    private Integer enabled = 1;

    private String remark;

    @NotEmpty(message = "至少选择一个角色")
    private List<Long> roleIds;
}
