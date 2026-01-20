package com.openx3.system.controller;

import com.openx3.common.common.R;
import com.openx3.system.domain.vo.OAuthTokenVO;
import com.openx3.system.service.ClientCredentialsAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2 Token 端点（Client Credentials）
 * 按文档提供：POST /oauth/token
 */
@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final ClientCredentialsAuthService authService;

    @PostMapping("/oauth/token")
    public R<OAuthTokenVO> token(@RequestParam("grant_type") String grantType,
                                @RequestParam("client_id") String clientId,
                                @RequestParam("client_secret") String clientSecret,
                                HttpServletRequest request) {
        String ip = resolveClientIp(request);
        return R.success(authService.token(grantType, clientId, clientSecret, ip));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // 取第一个
            return xff.split(",")[0].trim();
        }
        String xrip = request.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) {
            return xrip.trim();
        }
        return request.getRemoteAddr();
    }
}

