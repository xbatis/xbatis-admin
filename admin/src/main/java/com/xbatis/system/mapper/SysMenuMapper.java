package com.xbatis.system.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import com.xbatis.system.dto.MenuPageQuery;
import com.xbatis.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysMenuMapper extends MybatisMapper<SysMenu> {

    default Pager<SysMenu> pageMenus(MenuPageQuery query) {
        return QueryChain.of(this)
                .where(query)
                .orderBy(SysMenu::getParentId, SysMenu::getSort, SysMenu::getId)
                .paging(Pager.of(query.getCurrent(), query.getPageSize()));
    }
}
