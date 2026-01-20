package com.openx3.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.framework.mybatis.datascope.DataScope;
import com.openx3.system.entity.iam.SysEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysEmployeeMapper extends BaseMapper<SysEmployee> {

    /**
     * 示例：带数据权限过滤的查询（需要表中存在 dept_id/create_by 字段）
     */
    @DataScope(alias = "e")
    @Select("SELECT * FROM sys_employee e WHERE e.del_flag = 0")
    List<SysEmployee> selectAllWithScope();

    /**
     * 带数据权限过滤的分页查询
     */
    @DataScope(alias = "e")
    @Select("SELECT * FROM sys_employee e WHERE e.del_flag = 0")
    IPage<SysEmployee> selectPageWithScope(Page<SysEmployee> page);
}

