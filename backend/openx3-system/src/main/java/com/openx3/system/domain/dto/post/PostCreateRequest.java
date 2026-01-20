package com.openx3.system.domain.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostCreateRequest {

    @NotBlank(message = "tenantId不能为空")
    private String tenantId;

    @NotBlank(message = "postCode不能为空")
    private String postCode;

    @NotBlank(message = "postName不能为空")
    private String postName;

    /**
     * 1=正常 0=禁用
     */
    private Integer status = 1;
}

