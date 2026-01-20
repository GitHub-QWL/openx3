package com.openx3.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openx3.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    // 基础 CRUD 已自动拥有
}