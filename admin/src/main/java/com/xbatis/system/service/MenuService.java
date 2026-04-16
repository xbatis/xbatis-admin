package com.xbatis.system.service;

import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import com.xbatis.commons.api.PageResponse;
import com.xbatis.commons.exception.BusinessException;
import com.xbatis.commons.model.SystemConstants;
import com.xbatis.system.dto.MenuForm;
import com.xbatis.system.dto.MenuPageQuery;
import com.xbatis.system.entity.SysMenu;
import com.xbatis.system.entity.SysRole;
import com.xbatis.system.entity.SysRoleMenu;
import com.xbatis.system.entity.SysUserRole;
import com.xbatis.system.enums.MenuType;
import com.xbatis.system.mapper.SysMenuMapper;
import com.xbatis.system.mapper.SysRoleMapper;
import com.xbatis.system.mapper.SysRoleMenuMapper;
import com.xbatis.system.mapper.SysUserRoleMapper;
import com.xbatis.system.vo.MenuNodeVO;
import com.xbatis.system.vo.MenuPageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    public MenuService(
            SysMenuMapper menuMapper,
            SysRoleMapper roleMapper,
            SysRoleMenuMapper roleMenuMapper,
            SysUserRoleMapper userRoleMapper
    ) {
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public PageResponse<MenuPageVO> page(MenuPageQuery query) {
        Pager<SysMenu> pager = menuMapper.pageMenus(query);
        Map<Long, MenuNodeVO> menuIndex = pager.getResults().isEmpty() ? Map.of() : buildMenuIndex(loadAllMenus());
        List<MenuPageVO> records = pager.getResults().stream()
                .map(menu -> toPageVO(menu, menuIndex))
                .toList();
        return PageResponse.of(pager, records);
    }

    public List<MenuNodeVO> listTree() {
        return buildTree(loadAllMenus(), true);
    }

    public List<MenuNodeVO> listCurrentUserMenus(Long userId) {
        List<MenuNodeVO> menus = loadGrantedMenus(userId, false);
        return buildTree(menus, false);
    }

    public List<Long> listRoleMenuIds(Long roleId) {
        return QueryChain.of(roleMenuMapper)
                .select(SysRoleMenu::getMenuId)
                .eq(SysRoleMenu::getRoleId, roleId)
                .returnType(Long.class)
                .list();
    }

    public List<MenuNodeVO> listOptionsTree() {
        return listTree();
    }

    public MenuNodeVO detail(Long id) {
        MenuNodeVO menu = QueryChain.of(menuMapper)
                .eq(SysMenu::getId, id)
                .returnType(MenuNodeVO.class)
                .get();
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return menu;
    }

    @Transactional
    public Long create(MenuForm form) {
        validateMenuForm(null, form);
        SysMenu menu = toEntity(form, new SysMenu());
        menuMapper.save(menu);
        return menu.getId();
    }

    @Transactional
    public void update(Long id, MenuForm form) {
        SysMenu existing = menuMapper.getById(id);
        if (existing == null) {
            throw new BusinessException("菜单不存在");
        }
        validateMenuForm(id, form);
        menuMapper.update(toEntity(form, existing));
    }

    @Transactional
    public void delete(Long id) {
        SysMenu menu = menuMapper.getById(id);
        if (menu == null) {
            return;
        }
        int childCount = QueryChain.of(menuMapper)
                .eq(SysMenu::getParentId, id)
                .count();
        if (childCount > 0) {
            throw new BusinessException("请先删除子菜单后再删除当前菜单");
        }
        roleMenuMapper.delete(where -> where.eq(SysRoleMenu::getMenuId, id));
        menuMapper.deleteById(id);
    }

    private void validateMenuForm(Long id, MenuForm form) {
        if (id != null && id.equals(form.getParentId())) {
            throw new BusinessException("父菜单不能是自己");
        }
        if (form.getType() == MenuType.BUTTON && (form.getPermission() == null || form.getPermission().isBlank())) {
            throw new BusinessException("按钮权限必须填写 permission");
        }
    }

    private SysMenu toEntity(MenuForm form, SysMenu target) {
        target.setParentId(form.getParentId() == null ? SystemConstants.ROOT_MENU_PARENT_ID : form.getParentId());
        target.setName(form.getName());
        target.setType(form.getType().name());
        target.setPath(form.getPath());
        target.setComponent(form.getComponent());
        target.setIcon(form.getIcon());
        target.setPermission(form.getPermission());
        target.setRedirect(form.getRedirect());
        target.setSort(form.getSort() == null ? 0 : form.getSort());
        target.setVisible(form.getVisible() == null ? 1 : form.getVisible());
        target.setKeepAlive(form.getKeepAlive() == null ? 0 : form.getKeepAlive());
        target.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        target.setRemark(form.getRemark());
        return target;
    }

    private List<MenuNodeVO> loadAllMenus() {
        return QueryChain.of(menuMapper)
                .orderBy(SysMenu::getSort, SysMenu::getId)
                .returnType(MenuNodeVO.class)
                .list();
    }

    private List<MenuNodeVO> loadGrantedMenus(Long userId, boolean includeButtons) {
        List<Long> roleIds = QueryChain.of(userRoleMapper)
                .select(SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, userId)
                .returnType(Long.class)
                .list();
        if (roleIds.isEmpty()) {
            return List.of();
        }

        List<SysRole> roles = QueryChain.of(roleMapper)
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getEnabled, 1)
                .list();
        Set<Long> enabledRoleIds = roles.stream().map(SysRole::getId).collect(Collectors.toSet());

        List<MenuNodeVO> menus;
        if (enabledRoleIds.contains(SystemConstants.SUPER_ADMIN_ROLE_ID)) {
            menus = QueryChain.of(menuMapper)
                    .eq(SysMenu::getStatus, 1)
                    .orderBy(SysMenu::getSort, SysMenu::getId)
                    .returnType(MenuNodeVO.class)
                    .list();
        } else {
            List<Long> menuIds = QueryChain.of(roleMenuMapper)
                    .select(SysRoleMenu::getMenuId)
                    .in(SysRoleMenu::getRoleId, enabledRoleIds)
                    .returnType(Long.class)
                    .list();
            if (menuIds.isEmpty()) {
                return List.of();
            }
            menus = QueryChain.of(menuMapper)
                    .in(SysMenu::getId, menuIds)
                    .eq(SysMenu::getStatus, 1)
                    .orderBy(SysMenu::getSort, SysMenu::getId)
                    .returnType(MenuNodeVO.class)
                    .list();
        }

        return menus.stream()
                .filter(menu -> includeButtons || !MenuType.BUTTON.name().equals(menu.getType()))
                .sorted(Comparator.comparing(MenuNodeVO::getSort).thenComparing(MenuNodeVO::getId))
                .toList();
    }

    private List<MenuNodeVO> buildTree(List<MenuNodeVO> menus, boolean includeButtons) {
        List<MenuNodeVO> filtered = menus.stream()
                .filter(menu -> includeButtons || !MenuType.BUTTON.name().equals(menu.getType()))
                .sorted(Comparator.comparing(MenuNodeVO::getSort).thenComparing(MenuNodeVO::getId))
                .toList();
        Map<Long, List<MenuNodeVO>> childrenMap = buildChildrenMap(filtered);
        return buildChildren(SystemConstants.ROOT_MENU_PARENT_ID, childrenMap);
    }

    private List<MenuNodeVO> buildChildren(Long parentId, Map<Long, List<MenuNodeVO>> childrenMap) {
        List<MenuNodeVO> children = childrenMap.getOrDefault(parentId, List.of());
        List<MenuNodeVO> results = new ArrayList<>(children.size());
        for (MenuNodeVO child : children) {
            child.setChildren(buildChildren(child.getId(), childrenMap));
            results.add(child);
        }
        return results;
    }

    private Map<Long, List<MenuNodeVO>> buildChildrenMap(List<MenuNodeVO> menus) {
        return menus.stream()
                .collect(Collectors.groupingBy(
                        menu -> normalizeParentId(menu.getParentId()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private Map<Long, MenuNodeVO> buildMenuIndex(List<MenuNodeVO> menus) {
        return menus.stream()
                .collect(Collectors.toMap(
                        MenuNodeVO::getId,
                        menu -> menu,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    private MenuPageVO toPageVO(SysMenu menu, Map<Long, MenuNodeVO> menuIndex) {
        List<String> ancestors = resolveAncestors(menu.getParentId(), menuIndex);
        return MenuPageVO.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .name(menu.getName())
                .type(menu.getType())
                .path(menu.getPath())
                .component(menu.getComponent())
                .icon(menu.getIcon())
                .permission(menu.getPermission())
                .redirect(menu.getRedirect())
                .sort(menu.getSort())
                .visible(menu.getVisible())
                .keepAlive(menu.getKeepAlive())
                .status(menu.getStatus())
                .remark(menu.getRemark())
                .depth(ancestors.size())
                .parentPathLabel(ancestors.isEmpty() ? "根节点" : String.join(" / ", ancestors))
                .build();
    }

    private List<String> resolveAncestors(Long parentId, Map<Long, MenuNodeVO> menuIndex) {
        Long cursor = normalizeParentId(parentId);
        Set<Long> visited = new LinkedHashSet<>();
        List<String> ancestors = new ArrayList<>();

        while (!Objects.equals(cursor, SystemConstants.ROOT_MENU_PARENT_ID) && visited.add(cursor)) {
            MenuNodeVO parent = menuIndex.get(cursor);
            if (parent == null) {
                break;
            }
            ancestors.add(parent.getName());
            cursor = normalizeParentId(parent.getParentId());
        }

        Collections.reverse(ancestors);
        return ancestors;
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? SystemConstants.ROOT_MENU_PARENT_ID : parentId;
    }
}
