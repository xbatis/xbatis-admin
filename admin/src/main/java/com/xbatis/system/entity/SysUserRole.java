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
@Table("sys_user_role")
public class SysUserRole {

    @TableId(IdAutoType.AUTO)
    private Long id;

    private Long userId;

    private Long roleId;

    @TableField(defaultValue = "{NOW}")
    private LocalDateTime createdAt;
}
