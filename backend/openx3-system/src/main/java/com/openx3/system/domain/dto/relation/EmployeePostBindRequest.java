package com.openx3.system.domain.dto.relation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EmployeePostBindRequest {

    @NotBlank(message = "employeeId不能为空")
    private String employeeId;

    /**
     * 绑定的岗位ID列表（全量覆盖）
     */
    private List<String> postIds = new ArrayList<>();
}

