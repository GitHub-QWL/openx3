package com.openx3.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openx3.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    // 基础 CRUD 已自动拥有
}