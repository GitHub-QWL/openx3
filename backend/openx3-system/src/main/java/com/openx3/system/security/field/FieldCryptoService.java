package com.openx3.system.security.field;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 字段级加密服务（用于 ENCRYPT 策略）
 */
@Service
public class FieldCryptoService {

    @Value("${openx3.security.field-encrypt-key:openx3-dev-field-encrypt-key-change-me}")
    private String rawKey;

    private SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        // AES key 必须是 16/24/32 bytes。这里取 UTF-8 bytes 后做截断/补齐到 32。
        byte[] keyBytes = rawKey.getBytes(StandardCharsets.UTF_8);
        byte[] k = new byte[32];
        for (int i = 0; i < k.length; i++) {
            k[i] = i < keyBytes.length ? keyBytes[i] : 0;
        }
        this.secretKey = new SecretKeySpec(k, "AES");
    }

    public String encryptToBase64(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // output: base64(iv + cipher)
            byte[] out = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherBytes, 0, out, iv.length, cipherBytes.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            // 加密失败时不应泄漏明文，这里返回空串
            return "";
        }
    }
}

