package com.xbatis.auth.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import com.xbatis.auth.security.AppUserPrincipal;
import com.xbatis.commons.exception.BusinessException;
import com.xbatis.commons.model.SystemConstants;
import com.xbatis.system.entity.*;
import com.xbatis.system.mapper.*;
import com.xbatis.system.entity.*;
import com.xbatis.system.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthUserQueryService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public AuthUserQueryService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysMenuMapper menuMapper,
            SysUserRoleMapper userRoleMapper,
            SysRoleMenuMapper roleMenuMapper
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    public AppUserPrincipal loadByUsername(String username) {
        SysUser user = QueryChain.of(userMapper)
                .eq(SysUser::getUsername, username)
                .get();
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, 4013, "用户名或密码错误");
        }
        return buildPrincipal(user);
    }

    public AppUserPrincipal loadByUserId(Long userId) {
        SysUser user = userMapper.getById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, 4014, "用户不存在");
        }
        return buildPrincipal(user);
    }

    private AppUserPrincipal buildPrincipal(SysUser user) {
        List<SysRole> roles = QueryChain.of(userRoleMapper)
                .select(SysRole::getId, SysRole::getName)
                .innerJoin(SysUserRole::getRoleId, SysRole::getId)
                .eq(SysUserRole::getUserId, user.getId())
                .eq(SysRole::getEnabled, 1)
                .returnType(SysRole.class)
                .list();
        Set<Long> enabledRoleIds = new LinkedHashSet<>();
        Set<String> enableRoleNames = new LinkedHashSet<>();
        if (!roles.isEmpty()) {
            for (SysRole role : roles) {
                enabledRoleIds.add(role.getId());
                enableRoleNames.add(role.getName());
            }
        }

        Set<String> permissions = loadPermissions(enabledRoleIds);
        return new AppUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getDisplayName(),
                user.getEnabled(),
                enabledRoleIds,
                enableRoleNames,
                permissions
        );
    }

    private Set<String> loadPermissions(Set<Long> roleIds) {
        if (roleIds.contains(SystemConstants.SUPER_ADMIN_ROLE_ID)) {
            return QueryChain.of(menuMapper)

                    .eq(SysMenu::getStatus, 1)
                    .list()
                    .stream()
                    .map(SysMenu::getPermission)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        if (roleIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Long> menuIds = QueryChain.of(roleMenuMapper)
                .select(SysRoleMenu::getMenuId)
                .in(SysRoleMenu::getRoleId, roleIds)
                .returnType(Long.class)
                .list();
        if (menuIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return QueryChain.of(menuMapper)
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, 1)
                .list()
                .stream()
                .map(SysMenu::getPermission)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
