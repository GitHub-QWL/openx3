package com.openx3.system.domain.dto.dept;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeptUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    /**
     * 仅允许修改名称/排序/状态；如需变更父部门需单独实现移动逻辑以维护 tree_path
     */
    @NotBlank(message = "deptName不能为空")
    private String deptName;

    private Integer sortNo = 0;

    /**
     * 1=正常 0=禁用
     */
    private Integer status = 1;
}

