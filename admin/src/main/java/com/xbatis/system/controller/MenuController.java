package com.xbatis.system.controller;

import com.xbatis.commons.api.ApiResponse;
import com.xbatis.commons.api.PageResponse;
import com.xbatis.system.dto.MenuForm;
import com.xbatis.system.dto.MenuPageQuery;
import com.xbatis.system.service.MenuService;
import com.xbatis.system.vo.MenuNodeVO;
import com.xbatis.system.vo.MenuPageVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('sys:menu:view')")
    public ApiResponse<PageResponse<MenuPageVO>> page(@Valid MenuPageQuery query) {
        return ApiResponse.success(menuService.page(query));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:menu:view')")
    public ApiResponse<List<MenuNodeVO>> tree() {
        return ApiResponse.success(menuService.listTree());
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('sys:menu:view')")
    public ApiResponse<List<MenuNodeVO>> options() {
        return ApiResponse.success(menuService.listOptionsTree());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:view')")
    public ApiResponse<MenuNodeVO> detail(@PathVariable Long id) {
        return ApiResponse.success(menuService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:menu:create')")
    public ApiResponse<Long> create(@Valid @RequestBody MenuForm form) {
        return ApiResponse.success("创建成功", menuService.create(form));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:update')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody MenuForm form) {
        menuService.update(id, form);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
