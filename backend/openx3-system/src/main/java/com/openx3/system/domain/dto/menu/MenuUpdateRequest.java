package com.openx3.system.domain.dto.menu;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    private String parentId = "0";

    private String title;

    private String path;

    private String component;

    private String perms;

    private String icon;

    /**
     * 0=目录,1=菜单,2=按钮
     */
    private Integer type = 1;

    private Integer sortNo = 0;

    private Boolean visible = true;
}

