package com.openx3.system.domain.dto.relation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色自定义数据权限部门绑定（sys_role_dept）
 */
@Data
public class RoleDeptBindRequest {

    @NotBlank(message = "roleId不能为空")
    private String roleId;

    /**
     * 绑定的部门ID列表（全量覆盖）
     */
    private List<String> deptIds = new ArrayList<>();
}

