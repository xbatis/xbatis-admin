package com.xbatis.system.vo;

import cn.xbatis.db.annotations.Fetch;
import cn.xbatis.db.annotations.ResultEntity;
import com.xbatis.system.entity.SysRole;
import com.xbatis.system.entity.SysUser;
import com.xbatis.system.entity.SysUserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@ResultEntity(SysUser.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO {

    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String phone;
    private Integer enabled;
    private String remark;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    @Fetch(
            mergeGroup = "role",
            property = SysUser.Fields.id,
            middle = SysUserRole.class,
            middleSourceProperty = SysUserRole.Fields.userId,
            middleTargetProperty = SysUserRole.Fields.roleId,
            target = SysRole.class,
            targetProperty = SysRole.Fields.id,
            targetSelectProperty = SysRole.Fields.id
    )
    private List<Long> roleIds;

    @Fetch(
            mergeGroup = "role",
            property = SysUser.Fields.id,
            middle = SysUserRole.class,
            middleSourceProperty = SysUserRole.Fields.userId,
            middleTargetProperty = SysUserRole.Fields.roleId,
            target = SysRole.class,
            targetProperty = SysRole.Fields.id,
            targetSelectProperty = SysRole.Fields.name
    )
    private List<String> roleNames;
}
