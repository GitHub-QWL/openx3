package com.openx3.system.domain.dto.relation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoleMenuBindRequest {

    @NotBlank(message = "roleId不能为空")
    private String roleId;

    /**
     * 绑定的菜单ID列表（全量覆盖）
     */
    private List<String> menuIds = new ArrayList<>();
}

