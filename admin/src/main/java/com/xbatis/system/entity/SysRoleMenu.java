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
@Table("sys_role_menu")
public class SysRoleMenu {

    @TableId(IdAutoType.AUTO)
    private Long id;

    private Long roleId;

    private Long menuId;

    @TableField(defaultValue = "{NOW}")
    private LocalDateTime createdAt;
}
