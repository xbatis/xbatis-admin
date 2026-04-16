package com.xbatis.system.entity;

import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableField;
import cn.xbatis.db.annotations.TableId;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Data
@FieldNameConstants
@Table("sys_user")
public class SysUser {

    @TableId(IdAutoType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String displayName;

    private String email;

    private String phone;

    @TableField(defaultValue = "1")
    private Integer enabled;

    private String remark;

    private LocalDateTime lastLoginAt;

    @TableField(defaultValue = "{NOW}")
    private LocalDateTime createdAt;

    @TableField(defaultValue = "{NOW}", updateDefaultValue = "{NOW}")
    private LocalDateTime updatedAt;
}
