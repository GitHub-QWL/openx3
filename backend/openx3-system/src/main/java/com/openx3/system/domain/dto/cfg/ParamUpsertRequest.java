package com.openx3.system.domain.dto.cfg;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 参数 Upsert（按 paramCode）
 */
@Data
public class ParamUpsertRequest {

    @NotBlank(message = "paramCode不能为空")
    private String paramCode;

    private String paramName;

    private String paramValue;

    private String remark;
}

