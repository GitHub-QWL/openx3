package com.openx3.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openx3.core.entity.SysObject;
import com.openx3.core.entity.SysScript;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysObjectMapper extends BaseMapper<SysObject> {
    // MyBatis Plus 自动提供基础 CRUD
}