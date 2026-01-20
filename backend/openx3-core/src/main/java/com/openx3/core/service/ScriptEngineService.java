package com.openx3.core.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openx3.common.exception.BusinessException;
import com.openx3.core.entity.SysObject;
import com.openx3.core.entity.SysScript;
import com.openx3.core.mapper.SysScriptMapper;
import com.openx3.core.support.GenericDao;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy 动态脚本引擎服务 (AOP 责任链增强版)
 * <p>
 * 核心架构：
 * 1. 显式关联：通过 SysObject 配置标准脚本(STD)和二开脚本(SPE)
 * 2. 责任链执行：SPE (二开) -> STD (标准)
 * 3. 阻断机制：SPE 可通过设置 _NEXT=false 阻断后续标准逻辑
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScriptEngineService {

    private final SysScriptMapper scriptMapper;
    private final ApplicationContext applicationContext;
    private final GenericDao genericDao;

    // 编译缓存: ScriptCode -> Class
    private final Map<String, Class<?>> scriptCache = new ConcurrentHashMap<>();
    
    // Groovy 类加载器
    private GroovyClassLoader groovyClassLoader;

    @PostConstruct
    public void init() {
        log.info(">>> [ScriptEngine] 开始初始化...");
        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");
        // 初始化类加载器
        this.groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
        log.info(">>> [ScriptEngine] 引擎初始化完成 (AOP 模式)");
    }

    // =================================================
    //  管理接口逻辑 (CRUD & Publish)
    // =================================================

    /**
     * 保存脚本 (草稿)
     */
    public void saveScript(SysScript script) {
        if (!StringUtils.hasText(script.getCode())) {
            throw new BusinessException(400, "脚本编码不能为空");
        }

        SysScript exist = scriptMapper.selectOne(new LambdaQueryWrapper<SysScript>()
                .eq(SysScript::getCode, script.getCode()));

        if (exist != null) {
            script.setId(exist.getId());
            script.setUpdateTime(LocalDateTime.now());
            if (script.getVersion() == null) script.setVersion(exist.getVersion());
            scriptMapper.updateById(script);
        } else {
            script.setCreateTime(LocalDateTime.now());
            script.setUpdateTime(LocalDateTime.now());
            if (script.getVersion() == null) script.setVersion(1);
            scriptMapper.insert(script);
        }
    }

    /**
     * 发布脚本 (热更新)
     * 说明：清除缓存，下次执行时自动重新查库编译
     */
    public void publish(String scriptCode) {
        // 简单策略：直接移除缓存，下次用到时会自动懒加载
        scriptCache.remove(scriptCode);
        log.info(">>> [ScriptEngine] 脚本 [{}] 已发布 (缓存已清除)", scriptCode);
    }

    // =================================================
    //  核心执行逻辑 (AOP Chain)
    // =================================================

    /**
     * 执行 AOP 显式责任链
     * 逻辑流: SPE (二开) -> Check _NEXT -> STD (标准)
     *
     * @param meta 业务对象元数据 (包含脚本配置)
     * @param hookName 钩子方法名 (如 beforeSave, afterQuery)
     * @param context 上下文变量
     */
    public void executeExplicitChain(SysObject meta, String hookName, Map<String, Object> context) {
        // 1. 初始化 Binding (上下文)
        Binding binding = new Binding();
        if (context != null) context.forEach(binding::setVariable);
        
        // 注入通用能力
        binding.setVariable("log", log);
        binding.setVariable("db", genericDao);
        binding.setVariable("spring", applicationContext);
        
        // 控制变量: 默认为 true，脚本可将其设为 false 来阻断后续链条
        binding.setVariable("_NEXT", true);

        // 2. [Layer 1] 执行 SPE (Specific/二开)
        if (StringUtils.hasText(meta.getSpeScript())) {
            boolean success = executeScriptInternal(meta.getSpeScript(), hookName, binding);
            
            // 阻断判定：如果脚本显式设置 _NEXT = false，则阻断
            if (!success || Boolean.FALSE.equals(binding.getVariable("_NEXT"))) {
                log.info(">>> [AOP] SPE脚本 [{}] 阻断了后续逻辑 (Hook: {})", meta.getSpeScript(), hookName);
                return;
            }
        }

        // 3. [Layer 2] 执行 STD (Standard/标准)
        if (StringUtils.hasText(meta.getStdScript())) {
            executeScriptInternal(meta.getStdScript(), hookName, binding);
        }
    }

    /**
     * 内部执行单个脚本的指定方法
     * @return boolean true=执行成功/无异常, false=逻辑要求阻断(暂未启用)
     */
    private boolean executeScriptInternal(String scriptCode, String functionName, Binding binding) {
        // 1. 获取类 (懒加载 + 缓存)
        Class<?> scriptClass = getScriptClass(scriptCode);
        if (scriptClass == null) {
            log.warn(">>> [ScriptEngine] 脚本 [{}] 未找到或编译失败，跳过执行", scriptCode);
            return true; // 脚本缺失不应阻断流程
        }

        try {
            // 2. 实例化脚本对象 (多例，线程安全)
            Script scriptInstance = (Script) scriptClass.getDeclaredConstructor().newInstance();
            scriptInstance.setBinding(binding);

            // 3. 检查方法是否存在 (MetaClass)
            if (scriptInstance.getMetaClass().respondsTo(scriptInstance, functionName).isEmpty()) {
                // 没有定义该钩子方法，视为正常跳过
                return true; 
            }

            if (log.isDebugEnabled()) {
                log.debug(">>> [Exec] 执行脚本: {}#{}", scriptCode, functionName);
            }
            
            // 4. 反射调用方法
            Object result = scriptInstance.invokeMethod(functionName, null);

            // 5. 检查返回值 (可选增强：如果方法返回 false，也可以视为阻断)
            if (Boolean.FALSE.equals(result)) {
                binding.setVariable("_NEXT", false);
            }
            return true;

        } catch (Exception e) {
            // 脚本内部抛出的异常 (如 throw new RuntimeException("校验失败"))
            // 直接向上抛出，中断事务，回滚数据
            log.error(">>> [Error] 脚本执行异常: {}#{}", scriptCode, functionName, e);
            throw new BusinessException(500, e.getMessage()); // 将异常信息透传给前端
        }
    }

    /**
     * 获取脚本类 (Cache -> DB -> Compile)
     */
    private Class<?> getScriptClass(String code) {
        // 1. 查缓存
        if (scriptCache.containsKey(code)) {
            return scriptCache.get(code);
        }

        // 2. 查库
        SysScript scriptEntity = scriptMapper.selectOne(new LambdaQueryWrapper<SysScript>()
                .eq(SysScript::getCode, code));
        
        if (scriptEntity == null) return null;

        try {
            // 3. 动态编译
            // 为了避免类名冲突和支持热更新，给类名加个时间戳后缀
            // 脚本内容中通常不定义 package，Groovy 会自动生成类名，这里我们手动指定文件名来控制类名
            String className = code + "_" + System.currentTimeMillis();
            
            Class<?> clazz = groovyClassLoader.parseClass(scriptEntity.getContent(), className);
            scriptCache.put(code, clazz);
            return clazz;
        } catch (Exception e) {
            log.error(">>> [ScriptEngine] 编译失败: {}", code, e);
            throw new BusinessException(500, "脚本编译失败 [" + code + "]: " + e.getMessage());
        }
    }
}