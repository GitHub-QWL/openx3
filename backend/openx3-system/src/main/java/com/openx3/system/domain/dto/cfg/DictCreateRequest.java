package com.openx3.system.domain.dto.cfg;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictCreateRequest {

    @NotBlank(message = "dictCode不能为空")
    private String dictCode;

    @NotBlank(message = "dictName不能为空")
    private String dictName;

    /**
     * 1=启用 0=禁用
     */
    private Integer status = 1;

    private String remark;
}

