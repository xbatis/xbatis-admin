package com.xbatis.system.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import com.xbatis.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends MybatisMapper<SysUser> {
}
