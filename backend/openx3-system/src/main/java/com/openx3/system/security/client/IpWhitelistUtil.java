package com.openx3.system.security.client;

import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * IP 白名单校验（支持 IPv4 单 IP 与 CIDR）
 * 输入格式：逗号分隔，例如： "1.2.3.4,10.0.0.0/24"
 */
public class IpWhitelistUtil {

    private IpWhitelistUtil() {
    }

    public static boolean allow(String whitelist, String ip) {
        if (!StringUtils.hasText(whitelist)) return true;
        if (!StringUtils.hasText(ip)) return false;

        String[] items = whitelist.split(",");
        return Arrays.stream(items)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .anyMatch(item -> match(item, ip.trim()));
    }

    private static boolean match(String rule, String ip) {
        if (!StringUtils.hasText(rule)) return false;
        if (!StringUtils.hasText(ip)) return false;
        if (!rule.contains("/")) {
            return rule.equals(ip);
        }
        // CIDR
        String[] parts = rule.split("/");
        if (parts.length != 2) return false;
        String baseIp = parts[0].trim();
        int prefix;
        try {
            prefix = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (prefix < 0 || prefix > 32) return false;

        try {
            long ipLong = ipv4ToLong(ip);
            long baseLong = ipv4ToLong(baseIp);
            long mask = prefix == 0 ? 0 : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
            return (ipLong & mask) == (baseLong & mask);
        } catch (Exception e) {
            return false;
        }
    }

    private static long ipv4ToLong(String ip) throws UnknownHostException {
        InetAddress addr = InetAddress.getByName(ip);
        byte[] b = addr.getAddress();
        // 仅支持 IPv4
        if (b.length != 4) {
            throw new UnknownHostException("Only IPv4 supported");
        }
        return ((b[0] & 0xFFL) << 24)
                | ((b[1] & 0xFFL) << 16)
                | ((b[2] & 0xFFL) << 8)
                | (b[3] & 0xFFL);
    }
}

