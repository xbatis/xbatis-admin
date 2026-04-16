package com.xbatis.system.controller;

import com.xbatis.commons.api.ApiResponse;
import com.xbatis.commons.api.PageResponse;
import com.xbatis.system.dto.RoleForm;
import com.xbatis.system.dto.RolePageQuery;
import com.xbatis.system.service.RoleService;
import com.xbatis.system.vo.OptionVO;
import com.xbatis.system.vo.RoleVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:role:view')")
    public ApiResponse<PageResponse<RoleVO>> page(@Valid RolePageQuery query) {
        return ApiResponse.success(roleService.page(query));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('sys:user:view')")
    public ApiResponse<List<OptionVO>> options() {
        return ApiResponse.success(roleService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:view')")
    public ApiResponse<RoleVO> detail(@PathVariable Long id) {
        return ApiResponse.success(roleService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:create')")
    public ApiResponse<Long> create(@Valid @RequestBody RoleForm form) {
        return ApiResponse.success("创建成功", roleService.create(form));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:update')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RoleForm form) {
        roleService.update(id, form);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
