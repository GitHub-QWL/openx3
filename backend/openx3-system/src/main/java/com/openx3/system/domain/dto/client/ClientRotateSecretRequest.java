package com.openx3.system.domain.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 轮换 Client Secret
 */
@Data
public class ClientRotateSecretRequest {

    @NotBlank(message = "id不能为空")
    private String id;
}

