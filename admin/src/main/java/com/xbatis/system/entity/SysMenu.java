package com.xbatis.system.entity;

import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableField;
import cn.xbatis.db.annotations.TableId;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Data
@Table("sys_menu")
@FieldNameConstants
public class SysMenu {

    @TableId(IdAutoType.AUTO)
    private Long id;

    @TableField(defaultValue = "0")
    private Long parentId;

    private String name;

    private String type;

    private String path;

    private String component;

    private String icon;

    private String permission;

    private String redirect;

    @TableField(defaultValue = "0")
    private Integer sort;

    @TableField(defaultValue = "1")
    private Integer visible;

    @TableField(defaultValue = "0")
    private Integer keepAlive;

    @TableField(defaultValue = "1")
    private Integer status;

    private String remark;

    @TableField(defaultValue = "{NOW}")
    private LocalDateTime createdAt;

    @TableField(defaultValue = "{NOW}", updateDefaultValue = "{NOW}")
    private LocalDateTime updatedAt;
}
