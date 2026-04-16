package com.xbatis.auth.service;

import com.xbatis.auth.dto.CurrentUserProfile;
import com.xbatis.auth.dto.LoginRequest;
import com.xbatis.auth.dto.LoginResponse;
import com.xbatis.auth.security.AppUserPrincipal;
import com.xbatis.auth.security.JwtTokenService;
import com.xbatis.auth.security.SecurityUtils;
import com.xbatis.commons.exception.BusinessException;
import com.xbatis.system.entity.SysUser;
import com.xbatis.system.mapper.SysUserMapper;
import com.xbatis.system.service.MenuService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthUserQueryService authUserQueryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final SysUserMapper userMapper;
    private final MenuService menuService;

    public AuthService(
            AuthUserQueryService authUserQueryService,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            SysUserMapper userMapper,
            MenuService menuService
    ) {
        this.authUserQueryService = authUserQueryService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.userMapper = userMapper;
        this.menuService = menuService;
    }

    public LoginResponse login(LoginRequest request) {
        AppUserPrincipal principal = authUserQueryService.loadByUsername(request.getUsername());
        if (!principal.isEnabled()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 4031, "用户已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), principal.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, 4013, "用户名或密码错误");
        }

        SysUser update = new SysUser();
        update.setId(principal.getUserId());
        update.setLastLoginAt(LocalDateTime.now());
        userMapper.update(update);

        JwtTokenService.TokenPayload tokenPayload = jwtTokenService.createToken(principal.getUserId(), principal.getUsername());
        return LoginResponse.builder()
                .accessToken(tokenPayload.accessToken())
                .tokenType("Bearer")
                .expiresAt(tokenPayload.expiresAt())
                .build();
    }

    public CurrentUserProfile currentProfile() {
        AppUserPrincipal principal = SecurityUtils.currentUser();
        AppUserPrincipal fresh = authUserQueryService.loadByUserId(principal.getUserId());
        return CurrentUserProfile.builder()
                .userId(fresh.getUserId())
                .username(fresh.getUsername())
                .displayName(fresh.getDisplayName())
                .roleIds(fresh.getRoleIds())
                .roleNames(fresh.getRoleNames())
                .permissions(fresh.getPermissions())
                .menus(menuService.listCurrentUserMenus(fresh.getUserId()))
                .build();
    }
}
