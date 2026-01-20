package com.openx3.system.domain.dto.relation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostRoleBindRequest {

    @NotBlank(message = "postId不能为空")
    private String postId;

    /**
     * 绑定的角色ID列表（全量覆盖）
     */
    private List<String> roleIds = new ArrayList<>();
}

