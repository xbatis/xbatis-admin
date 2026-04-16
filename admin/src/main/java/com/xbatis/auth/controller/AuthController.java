package com.xbatis.auth.controller;

import com.xbatis.auth.dto.CurrentUserProfile;
import com.xbatis.auth.dto.LoginRequest;
import com.xbatis.auth.dto.LoginResponse;
import com.xbatis.auth.service.AuthService;
import com.xbatis.commons.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/profile")
    public ApiResponse<CurrentUserProfile> profile() {
        return ApiResponse.success(authService.currentProfile());
    }
}
