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
@Table("sys_role")
public class SysRole {

    @TableId(IdAutoType.AUTO)
    private Long id;

    private String name;

    @TableField(defaultValue = "1")
    private Integer enabled;

    @TableField(defaultValue = "0")
    private Integer sort;

    @TableField(defaultValue = "1")
    private Integer deletable;

    private String remark;

    @TableField(defaultValue = "{NOW}")
    private LocalDateTime createdAt;

    @TableField(defaultValue = "{NOW}", updateDefaultValue = "{NOW}")
    private LocalDateTime updatedAt;
}
