package com.openx3.system.security.field;

import com.openx3.system.domain.model.AuthTokenContext;
import com.openx3.system.entity.SysFieldPolicy;
import com.openx3.system.entity.SysRole;
import com.openx3.system.security.AuthContextHolder;
import com.openx3.system.service.IamRbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 字段权限拦截（在 JSON 序列化前最后一刻处理）
 * - HIDDEN: 输出为 null（Map 场景直接移除 key）
 * - MASK: 字符串脱敏
 * - ENCRYPT: 字符串 AES-GCM 加密后 Base64 输出
 */
@Component
@RestControllerAdvice
@RequiredArgsConstructor
public class FieldPolicyResponseAdvice implements ResponseBodyAdvice<Object> {

    private final IamRbacService rbacService;
    private final FieldCryptoService cryptoService;
    private final FieldPolicyService fieldPolicyService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body == null) return null;
        if (!MediaType.APPLICATION_JSON.includes(selectedContentType)) return body;

        AuthTokenContext ctx;
        try {
            ctx = AuthContextHolder.getNullable();
        } catch (Exception e) {
            return body;
        }
        if (ctx == null) return body;

        String resourceCode = resolveResourceCode(returnType, request);
        if (!StringUtils.hasText(resourceCode)) return body;

        List<SysRole> roles = rbacService.listRoles(ctx);
        if (roles.isEmpty()) return body;
        Set<String> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toSet());

        applyPolicies(body, fieldPolicyService.getMergedPolicies(roleIds, resourceCode));
        return body;
    }

    private String resolveResourceCode(MethodParameter returnType, ServerHttpRequest request) {
        FieldPolicyResource ann = returnType.getMethodAnnotation(FieldPolicyResource.class);
        if (ann == null && returnType.getContainingClass() != null) {
            ann = returnType.getContainingClass().getAnnotation(FieldPolicyResource.class);
        }
        if (ann != null && StringUtils.hasText(ann.value())) {
            return ann.value();
        }

        // 运行时接口兜底：/api/runtime/{objectCode}/{action} -> {objectCode}_{action}
        String path = request.getURI().getPath();
        if (!StringUtils.hasText(path)) return null;
        String[] parts = path.split("/");
        if (parts.length >= 5 && "api".equals(parts[1]) && "runtime".equals(parts[2])) {
            String objectCode = parts[3];
            String action = parts[4];
            return objectCode.toLowerCase(Locale.ROOT) + "_" + action.toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private void applyPolicies(Object body, Map<String, SysFieldPolicy> policies) {
        if (body == null || policies == null || policies.isEmpty()) return;

        if (body instanceof Map<?, ?>) {
            applyToMap((Map<?, ?>) body, policies);
            return;
        }
        if (body instanceof Collection<?> c) {
            for (Object item : c) {
                applyPolicies(item, policies);
            }
            return;
        }

        // POJO：反射设置字段
        applyToPojo(body, policies);
    }

    private void applyToMap(Map<?, ?> map, Map<String, SysFieldPolicy> policies) {
        if (!(map instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<Object, Object> m = (Map<Object, Object>) map;

        for (Map.Entry<String, SysFieldPolicy> e : policies.entrySet()) {
            String field = e.getKey();
            if (!m.containsKey(field)) continue;
            SysFieldPolicy p = e.getValue();
            String pol = p.getPolicy();
            if (pol == null) continue;

            String up = pol.toUpperCase(Locale.ROOT);
            if ("HIDDEN".equals(up)) {
                m.remove(field);
            } else if ("MASK".equals(up)) {
                Object v = m.get(field);
                if (v instanceof String s) {
                    m.put(field, FieldMaskUtil.mask(s));
                }
            } else if ("ENCRYPT".equals(up)) {
                Object v = m.get(field);
                if (v instanceof String s) {
                    m.put(field, cryptoService.encryptToBase64(s));
                }
            }
        }
    }

    private void applyToPojo(Object bean, Map<String, SysFieldPolicy> policies) {
        Class<?> clazz = bean.getClass();
        for (Map.Entry<String, SysFieldPolicy> e : policies.entrySet()) {
            String fieldName = e.getKey();
            SysFieldPolicy p = e.getValue();
            if (p.getPolicy() == null) continue;

            Field f = findField(clazz, fieldName);
            if (f == null) continue;
            try {
                f.setAccessible(true);
                Object v = f.get(bean);
                String up = p.getPolicy().toUpperCase(Locale.ROOT);
                if ("HIDDEN".equals(up)) {
                    f.set(bean, null);
                } else if ("MASK".equals(up) && v instanceof String s) {
                    f.set(bean, FieldMaskUtil.mask(s));
                } else if ("ENCRYPT".equals(up) && v instanceof String s) {
                    f.set(bean, cryptoService.encryptToBase64(s));
                }
            } catch (Exception ignored) {
                // 不影响主流程
            }
        }
    }

    private Field findField(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
    }
}

