package com.xbatis.auth.security;

import com.xbatis.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AppUserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, 4010, "未登录或登录已失效");
        }
        return (AppUserPrincipal) authentication.getPrincipal();
    }
}
