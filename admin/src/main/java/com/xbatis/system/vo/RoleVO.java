package com.xbatis.system.vo;

import cn.xbatis.db.annotations.Fetch;
import cn.xbatis.db.annotations.ResultEntity;
import com.xbatis.system.entity.SysRole;
import com.xbatis.system.entity.SysRoleMenu;
import com.xbatis.system.entity.SysUserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@ResultEntity(SysRole.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleVO {

    private Long id;
    private String name;
    private Integer enabled;
    private Integer sort;
    private Integer deletable;
    private String remark;
    private LocalDateTime createdAt;

    @Fetch(
            property = SysRole.Fields.id,
            target = SysRoleMenu.class,
            targetProperty = SysRoleMenu.Fields.roleId,
            targetSelectProperty = SysRoleMenu.Fields.menuId
    )
    private List<Long> menuIds;

    @Fetch(
            property = SysRole.Fields.id,
            target = SysUserRole.class,
            targetProperty = SysUserRole.Fields.roleId,
            targetSelectProperty = "[count({id})]"
    )
    private Integer userCount;
}
