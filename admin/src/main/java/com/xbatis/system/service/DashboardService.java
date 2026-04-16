package com.xbatis.system.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import com.xbatis.system.entity.SysUser;
import com.xbatis.system.mapper.SysMenuMapper;
import com.xbatis.system.mapper.SysRoleMapper;
import com.xbatis.system.mapper.SysUserMapper;
import com.xbatis.system.vo.DashboardOverviewVO;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    public DashboardService(SysUserMapper userMapper, SysRoleMapper roleMapper, SysMenuMapper menuMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
    }

    public DashboardOverviewVO overview() {
        return DashboardOverviewVO.builder()
                .userCount(QueryChain.of(userMapper).count())
                .roleCount(QueryChain.of(roleMapper).count())
                .menuCount(QueryChain.of(menuMapper).count())
                .enabledUserCount(QueryChain.of(userMapper).eq(SysUser::getEnabled, 1).count())
                .build();
    }
}
