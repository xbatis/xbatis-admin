package com.xbatis.system.controller;

import com.xbatis.commons.api.ApiResponse;
import com.xbatis.commons.api.PageResponse;
import com.xbatis.system.dto.UserForm;
import com.xbatis.system.dto.UserPageQuery;
import com.xbatis.system.service.UserService;
import com.xbatis.system.vo.UserVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/system/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:view')")
    public ApiResponse<PageResponse<UserVO>> page(@Valid UserPageQuery query) {
        try {
            return ApiResponse.success(userService.page(query));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(5000, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:view')")
    public ApiResponse<UserVO> detail(@PathVariable Long id) {
        return ApiResponse.success(userService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:create')")
    public ApiResponse<Long> create(@Valid @RequestBody UserForm form) {
        return ApiResponse.success("创建成功", userService.create(form));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:update')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UserForm form) {
        userService.update(id, form);
        return ApiResponse.success("更新成功", null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:user:status')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusBody body) {
        userService.updateStatus(id, body.getEnabled());
        return ApiResponse.success("状态更新成功", null);
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('sys:user:reset-password')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return ApiResponse.success("密码已重置为默认值", null);
    }

    @Data
    public static class UserStatusBody {
        @NotNull(message = "enabled 不能为空")
        private Integer enabled;
    }
}
