package com.openx3.system.domain.dto.cfg;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictItemUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    @NotBlank(message = "itemLabel不能为空")
    private String itemLabel;

    private Integer sortNo = 0;

    /**
     * 1=启用 0=禁用
     */
    private Integer status = 1;
}

