package com.openx3.system.domain.dto.cfg;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocalMenuUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    private String parentId = "0";

    private String title;

    private String functionCode;

    private String url;

    private String icon;

    private Integer sortNo = 0;

    /**
     * 1=启用 0=禁用
     */
    private Integer status = 1;
}

