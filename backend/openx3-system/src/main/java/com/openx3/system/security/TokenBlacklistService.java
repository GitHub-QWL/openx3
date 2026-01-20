package com.openx3.system.security;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token 黑名单（用于强制注销/踢下线）
 * 与文档一致：服务端不存 Session，但通过 Redis 黑名单可控强制注销。
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "openx3:token:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void blacklistCurrentToken() {
        String token = StpUtil.getTokenValue();
        if (token == null || token.isEmpty()) {
            return;
        }

        long timeout = StpUtil.getTokenTimeout(); // seconds
        // -1 表示永不过期，这里给一个安全上限（30天），避免黑名单永久膨胀
        long seconds = timeout > 0 ? timeout : 30L * 24 * 60 * 60;

        redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", Duration.ofSeconds(seconds));
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) return false;
        Boolean exists = redisTemplate.hasKey(KEY_PREFIX + token);
        return Boolean.TRUE.equals(exists);
    }
}

