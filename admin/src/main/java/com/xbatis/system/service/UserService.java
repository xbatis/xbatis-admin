package com.xbatis.system.service;

import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import com.xbatis.commons.api.PageResponse;
import com.xbatis.commons.exception.BusinessException;
import com.xbatis.commons.model.SystemConstants;
import com.xbatis.system.dto.UserForm;
import com.xbatis.system.dto.UserPageQuery;
import com.xbatis.system.entity.SysRole;
import com.xbatis.system.entity.SysUser;
import com.xbatis.system.entity.SysUserRole;
import com.xbatis.system.mapper.SysRoleMapper;
import com.xbatis.system.mapper.SysUserMapper;
import com.xbatis.system.mapper.SysUserRoleMapper;
import com.xbatis.system.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysUserRoleMapper userRoleMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResponse<UserVO> page(UserPageQuery query) {
        Pager<UserVO> pager = QueryChain.of(userMapper)
                .like(SysUser::getUsername, query.getKeyword(), StringUtils::hasText)
                .eq(SysUser::getEnabled, query.getEnabled(), Objects::nonNull)
                .orderByDesc(SysUser::getId)
                .returnType(UserVO.class)
                .paging(Pager.of(query.getCurrent(), query.getPageSize()));
        return PageResponse.of(pager, pager.getResults());
    }

    public UserVO detail(Long id) {
        UserVO user = QueryChain.of(userMapper)
                .eq(SysUser::getId, id)
                .returnType(UserVO.class)
                .get();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Transactional
    public Long create(UserForm form) {
        ensureUsernameUnique(null, form.getUsername());
        SysUser user = new SysUser();
        user.setUsername(form.getUsername());
        user.setDisplayName(form.getDisplayName());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setEnabled(form.getEnabled() == null ? 1 : form.getEnabled());
        user.setRemark(form.getRemark());
        user.setPassword(passwordEncoder.encode(SystemConstants.DEFAULT_RESET_PASSWORD));
        userMapper.save(user);
        syncRoles(user.getId(), form.getRoleIds());
        return user.getId();
    }

    @Transactional
    public void update(Long id, UserForm form) {
        SysUser existing = userMapper.getById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        ensureUsernameUnique(id, form.getUsername());
        existing.setUsername(form.getUsername());
        existing.setDisplayName(form.getDisplayName());
        existing.setEmail(form.getEmail());
        existing.setPhone(form.getPhone());
        existing.setEnabled(form.getEnabled() == null ? 1 : form.getEnabled());
        existing.setRemark(form.getRemark());
        userMapper.update(existing);
        syncRoles(id, form.getRoleIds());
    }

    public void updateStatus(Long id, Integer enabled) {
        SysUser existing = userMapper.getById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        existing.setEnabled(enabled);
        userMapper.update(existing);
    }

    public void resetPassword(Long id) {
        SysUser existing = userMapper.getById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(SystemConstants.DEFAULT_RESET_PASSWORD));
        userMapper.update(user);
    }

    private void ensureUsernameUnique(Long id, String username) {
        SysUser duplicated = QueryChain.of(userMapper)
                .eq(SysUser::getUsername, username)
                .get();
        if (duplicated != null && !Objects.equals(duplicated.getId(), id)) {
            throw new BusinessException("用户名已存在");
        }
    }

    private void syncRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(where -> where.eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueRoleIds = new LinkedHashSet<>(roleIds);
        List<Long> existingRoleIds = QueryChain.of(roleMapper)
                .select(SysRole::getId)
                .in(SysRole::getId, uniqueRoleIds)
                .returnType(Long.class)
                .list();
        if (existingRoleIds.size() != uniqueRoleIds.size()) {
            throw new BusinessException("存在无效角色，请刷新后重试");
        }
        for (Long roleId : uniqueRoleIds) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.save(relation);
        }
    }

    private Map<Long, List<Long>> buildUserRoleIdsMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<Long>> result = new LinkedHashMap<>();
        QueryChain.of(userRoleMapper)
                .in(SysUserRole::getUserId, userIds)
                .list()
                .forEach(relation -> result.computeIfAbsent(relation.getUserId(), key -> new ArrayList<>()).add(relation.getRoleId()));
        return result;
    }

}
