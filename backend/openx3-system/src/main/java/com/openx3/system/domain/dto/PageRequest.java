package com.openx3.system.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页请求
 */
@Data
public class PageRequest {

    @Min(value = 1, message = "pageNo最小为1")
    private long pageNo = 1;

    @Min(value = 1, message = "pageSize最小为1")
    @Max(value = 500, message = "pageSize最大为500")
    private long pageSize = 20;
}

