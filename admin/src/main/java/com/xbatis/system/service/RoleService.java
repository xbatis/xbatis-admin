package com.xbatis.system.service;

import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import com.xbatis.commons.api.PageResponse;
import com.xbatis.commons.exception.BusinessException;
import com.xbatis.system.dto.RoleForm;
import com.xbatis.system.dto.RolePageQuery;
import com.xbatis.system.entity.SysRole;
import com.xbatis.system.entity.SysRoleMenu;
import com.xbatis.system.entity.SysUserRole;
import com.xbatis.system.mapper.SysRoleMapper;
import com.xbatis.system.mapper.SysRoleMenuMapper;
import com.xbatis.system.mapper.SysUserRoleMapper;
import com.xbatis.system.vo.OptionVO;
import com.xbatis.system.vo.RoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    public RoleService(
            SysRoleMapper roleMapper,
            SysRoleMenuMapper roleMenuMapper,
            SysUserRoleMapper userRoleMapper
    ) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public PageResponse<RoleVO> page(RolePageQuery query) {
        Pager<RoleVO> pager = QueryChain.of(roleMapper)
                .like(SysRole::getName, query.getKeyword(), StringUtils::hasText)
                .eq(SysRole::getEnabled, query.getEnabled(), Objects::nonNull)
                .orderBy(SysRole::getSort, SysRole::getId)
                .returnType(RoleVO.class)
                .paging(Pager.of(query.getCurrent(), query.getPageSize()));
        return PageResponse.of(pager, pager.getResults());
    }

    public List<OptionVO> options() {
        return QueryChain.of(roleMapper)
                .eq(SysRole::getEnabled, 1)
                .orderBy(SysRole::getSort, SysRole::getId)
                .returnType(OptionVO.class)
                .list();
    }

    public RoleVO detail(Long id) {
        RoleVO role = QueryChain.of(roleMapper)
                .eq(SysRole::getId, id)
                .returnType(RoleVO.class)
                .get();
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    @Transactional
    public Long create(RoleForm form) {
        validateUnique(null, form);
        SysRole role = new SysRole();
        applyForm(role, form);
        roleMapper.save(role);
        syncRoleMenus(role.getId(), form.getMenuIds());
        return role.getId();
    }

    @Transactional
    public void update(Long id, RoleForm form) {
        SysRole role = roleMapper.getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        validateUnique(id, form);
        applyForm(role, form);
        roleMapper.update(role);
        syncRoleMenus(id, form.getMenuIds());
    }

    @Transactional
    public void delete(Long id) {
        SysRole role = roleMapper.getById(id);
        if (role == null) {
            return;
        }
        int assignedUsers = QueryChain.of(userRoleMapper)
                .eq(SysUserRole::getRoleId, id)
                .count();
        if (assignedUsers > 0) {
            throw new BusinessException("角色已分配给用户，不能直接删除");
        }
        if (!Objects.equals(role.getDeletable(), 1)) {
            throw new BusinessException("当前角色已锁定，不允许删除");
        }
        roleMenuMapper.delete(where -> where.eq(SysRoleMenu::getRoleId, id));
        roleMapper.deleteById(id);
    }

    private void validateUnique(Long id, RoleForm form) {
        List<SysRole> roles = QueryChain.of(roleMapper)
                .list();
        boolean duplicated = roles.stream().anyMatch(role ->
                !Objects.equals(role.getId(), id) &&
                        Objects.equals(role.getName(), form.getName())
        );
        if (duplicated) {
            throw new BusinessException("角色名称已存在");
        }
    }

    private void applyForm(SysRole role, RoleForm form) {
        role.setName(form.getName());
        role.setEnabled(form.getEnabled());
        role.setSort(form.getSort() == null ? 0 : form.getSort());
        role.setDeletable(form.getDeletable() == null ? 1 : form.getDeletable());
        role.setRemark(form.getRemark());
    }

    private void syncRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(where -> where.eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueMenuIds = new LinkedHashSet<>(menuIds);
        for (Long menuId : uniqueMenuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuMapper.save(relation);
        }
    }
}
