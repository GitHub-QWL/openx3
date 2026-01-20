package com.openx3.system.domain.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    @NotBlank(message = "postName不能为空")
    private String postName;

    /**
     * 1=正常 0=禁用
     */
    private Integer status = 1;
}

