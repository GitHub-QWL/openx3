package com.openx3.system.security.field;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openx3.system.entity.SysFieldPolicy;
import com.openx3.system.mapper.SysFieldPolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段权限策略查询服务（带轻量缓存）
 */
@Service
@RequiredArgsConstructor
public class FieldPolicyService {

    private static final Duration TTL = Duration.ofSeconds(60);

    private final SysFieldPolicyMapper mapper;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public void clearCache() {
        cache.clear();
    }

    public Map<String, SysFieldPolicy> getMergedPolicies(Set<String> roleIds, String resourceCode) {
        if (roleIds == null || roleIds.isEmpty() || !StringUtils.hasText(resourceCode)) {
            return Map.of();
        }

        String key = buildCacheKey(roleIds, resourceCode);
        CacheEntry cached = cache.get(key);
        long now = System.currentTimeMillis();
        if (cached != null && (now - cached.ts) < TTL.toMillis()) {
            return cached.data;
        }

        List<SysFieldPolicy> policies = mapper.selectList(new LambdaQueryWrapper<SysFieldPolicy>()
                .in(SysFieldPolicy::getRoleId, roleIds)
                .eq(SysFieldPolicy::getResourceCode, resourceCode)
                .eq(SysFieldPolicy::getDelFlag, 0));

        Map<String, SysFieldPolicy> merged = mergePolicies(policies);
        cache.put(key, new CacheEntry(now, merged));
        return merged;
    }

    private String buildCacheKey(Set<String> roleIds, String resourceCode) {
        List<String> ids = new ArrayList<>(roleIds);
        Collections.sort(ids);
        return resourceCode + "|" + String.join(",", ids);
    }

    private Map<String, SysFieldPolicy> mergePolicies(List<SysFieldPolicy> policies) {
        if (policies == null || policies.isEmpty()) return Map.of();

        // 同一字段多个角色策略时，取最严格：HIDDEN > MASK > ENCRYPT
        Map<String, SysFieldPolicy> map = new LinkedHashMap<>();
        for (SysFieldPolicy p : policies) {
            if (!StringUtils.hasText(p.getFieldName()) || !StringUtils.hasText(p.getPolicy())) continue;
            String field = p.getFieldName();
            SysFieldPolicy old = map.get(field);
            if (old == null) {
                map.put(field, p);
                continue;
            }
            if (priority(p.getPolicy()) > priority(old.getPolicy())) {
                map.put(field, p);
            }
        }
        return map;
    }

    private int priority(String policy) {
        if (policy == null) return 0;
        return switch (policy.toUpperCase(Locale.ROOT)) {
            case "HIDDEN" -> 3;
            case "MASK" -> 2;
            case "ENCRYPT" -> 1;
            default -> 0;
        };
    }

    private record CacheEntry(long ts, Map<String, SysFieldPolicy> data) {
    }
}

