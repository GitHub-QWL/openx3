package com.openx3.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openx3.common.common.R;
import com.openx3.core.entity.SysScript;
import com.openx3.core.mapper.SysScriptMapper;
import com.openx3.core.service.ScriptEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 脚本管理接口
 * 归属模块: openx3-core (确保 JPA/MP 隔离)
 */
@RestController
@RequestMapping("/api/sys/script")
@RequiredArgsConstructor
public class SysScriptController {

    private final ScriptEngineService scriptEngineService;
    private final SysScriptMapper scriptMapper; // 用于简单的查询

    /**
     * 保存脚本 (Ctrl+S)
     */
    @PostMapping("/save")
    public R<String> save(@RequestBody SysScript script) {
        scriptEngineService.saveScript(script);
        return R.success("保存成功");
    }

    /**
     * 发布脚本 (热编译)
     */
    @PostMapping("/publish/{code}")
    public R<String> publish(@PathVariable String code) {
        scriptEngineService.publish(code);
        return R.success("发布成功，已生效");
    }

    /**
     * 获取详情
     */
    @GetMapping("/{code}")
    public R<SysScript> getInfo(@PathVariable String code) {
        // 这里可以直接用 Mapper 查询，因为 Controller 和 Mapper 同在 Core 模块
        SysScript script = scriptMapper.selectOne(new LambdaQueryWrapper<SysScript>()
                .eq(SysScript::getCode, code));
        return R.success(script);
    }
}