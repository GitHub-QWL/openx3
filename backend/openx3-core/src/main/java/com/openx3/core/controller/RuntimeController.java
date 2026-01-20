package com.openx3.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openx3.common.common.R;
import com.openx3.core.entity.SysObject;
import com.openx3.core.mapper.SysObjectMapper;
import com.openx3.core.service.ScriptEngineService;
import com.openx3.core.support.GenericDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用运行时接口
 * 路由格式: /api/runtime/{objectCode}/{action}
 */
@Slf4j
@RestController
@RequestMapping("/api/runtime")
@RequiredArgsConstructor
public class RuntimeController {

    private final SysObjectMapper objectMapper;
    private final GenericDao genericDao;
    private final ScriptEngineService scriptEngine;

    /**
     * 通用查询接口
     */
    @PostMapping("/{objectCode}/list")
    public R<List<Map<String, Object>>> list(
            @PathVariable String objectCode, 
            @RequestBody(required = false) Map<String, Object> params
    ) {
        if (params == null) params = new HashMap<>();
        
        SysObject meta = objectMapper.selectOne(new LambdaQueryWrapper<SysObject>().eq(SysObject::getCode, objectCode));
        if (meta == null) return R.error("业务对象不存在");

        // [AOP] beforeQuery
        Map<String, Object> context = new HashMap<>();
        context.put("params", params);
        scriptEngine.executeExplicitChain(meta, "beforeQuery", context);

        // SQL 构造 (简化版)
        String sql = "SELECT * FROM " + meta.getTableName();
        if (params.containsKey("id")) {
            sql += " WHERE id = :id";
        } else {
            sql += " LIMIT 100";
        }

        List<Map<String, Object>> result = genericDao.findList(sql, params);

        // [AOP] afterQuery (处理结果集，如脱敏)
        context.put("result", result);
        scriptEngine.executeExplicitChain(meta, "afterQuery", context);
        
        return R.success(result);
    }

    /**
     * 通用保存接口 (新增/修改)
     * 逻辑: SPE -> STD -> DB -> SPE -> STD
     */
    @PostMapping("/{objectCode}/save")
    public R<String> save(@PathVariable String objectCode, @RequestBody Map<String, Object> data) {
        // 1. 获取元数据 (包含脚本配置)
        SysObject meta = objectMapper.selectOne(new LambdaQueryWrapper<SysObject>().eq(SysObject::getCode, objectCode));
        if (meta == null) return R.error("业务对象不存在: " + objectCode);

        // 2. 准备上下文
        Map<String, Object> context = new HashMap<>();
        context.put("data", data); // 核心数据

        // 3. [AOP] 执行前置责任链 (beforeSave)
        // 引擎会自动处理 SPE 阻断逻辑
        scriptEngine.executeExplicitChain(meta, "beforeSave", context);

        // 4. [DB] 执行物理保存
        genericDao.save(meta.getTableName(), data);

        // 5. [AOP] 执行后置责任链 (afterSave)
        scriptEngine.executeExplicitChain(meta, "afterSave", context);

        return R.success("保存成功");
    }


    /**
     * 通用删除接口
     * 逻辑: 查原数据 -> SPE(beforeDelete) -> STD(beforeDelete) -> 物理删除 -> SPE(afterDelete) -> STD(afterDelete)
     *
     * @param objectCode 业务对象编码
     * @param id 主键ID
     */
    @PostMapping("/{objectCode}/delete")
    public R<String> delete(@PathVariable String objectCode, @RequestParam String id) {
        // 1. 获取元数据
        SysObject meta = objectMapper.selectOne(new LambdaQueryWrapper<SysObject>().eq(SysObject::getCode, objectCode));
        if (meta == null) return R.error("业务对象不存在: " + objectCode);

        // 2. [关键步骤] 删除前先查询原数据
        // 目的：将完整数据传给脚本，以便脚本根据状态(status)等字段判断是否允许删除
        String sql = "SELECT * FROM " + meta.getTableName() + " WHERE id = :id";
        Map<String, Object> existingData = genericDao.findOne(sql, java.util.Collections.singletonMap("id", id));
        
        if (existingData == null) {
            return R.error("数据不存在或已被删除");
        }

        // 3. 准备上下文
        Map<String, Object> context = new HashMap<>();
        context.put("id", id);
        context.put("data", existingData); // 将原始数据放入上下文，供脚本使用

        // 4. [AOP] 执行前置责任链 (beforeDelete)
        // 常见用途：权限校验、业务状态检查、级联检查
        scriptEngine.executeExplicitChain(meta, "beforeDelete", context);

        // 5. [DB] 执行物理删除
        genericDao.delete(meta.getTableName(), id);

        // 6. [AOP] 执行后置责任链 (afterDelete)
        // 常见用途：删除关联表数据、记录审计日志、同步删除第三方系统
        scriptEngine.executeExplicitChain(meta, "afterDelete", context);

        return R.success("删除成功");
    }
}