package com.xbatis.auth.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AppUserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String displayName;
    private final Integer enabled;
    private final Set<Long> roleIds;
    private final Set<String> roleNames;
    private final Set<String> permissions;

    public AppUserPrincipal(
            Long userId,
            String username,
            String password,
            String displayName,
            Integer enabled,
            Set<Long> roleIds,
            Set<String> roleNames,
            Set<String> permissions
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.enabled = enabled;
        this.roleIds = roleIds;
        this.roleNames = roleNames;
        this.permissions = permissions;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> auths = new LinkedHashSet<>();
        if (roleIds != null) {
            roleIds.forEach(roleId -> auths.add("ROLE_" + roleId));
        }
        if (permissions != null) {
            auths.addAll(permissions);
        }
        return auths.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled == 1;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }
}
