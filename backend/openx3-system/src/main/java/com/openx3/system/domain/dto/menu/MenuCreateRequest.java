package com.openx3.system.domain.dto.menu;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 菜单创建请求对象
 *
 * 用于接收前端传递的菜单创建请求参数，定义了菜单的基本属性和配置信息。
 *
 * @author author_name
 * @date 2026-01-20
 * @since 1.0.0
 */
@Data
public class MenuCreateRequest {

    /**
     * 父级菜单ID，默认为"0"表示顶级菜单
     */
    private String parentId = "0";

    /**
     * 菜单标题，用于显示在导航菜单中的文本
     */
    private String title;

    /**
     * 路由路径，用于前端路由匹配和跳转
     */
    private String path;

    /**
     * 组件路径，指定该路由对应的前端组件
     */
    private String component;

    /**
     * 权限标识，用于后端权限验证和前端按钮/菜单显示控制
     */
    private String perms;

    /**
     * 图标，用于在菜单项前显示的图标
     */
    private String icon;

    /**
     * 菜单类型：0=目录,1=菜单,2=按钮
     * 目录：只做分组，不可点击跳转
     * 菜单：可以点击跳转到对应页面
     * 按钮：页面内的操作按钮，用于权限控制
     */
    private Integer type = 1;

    /**
     * 排序号，用于同一父级下菜单项的排序，默认为0
     */
    private Integer sortNo = 0;

    /**
     * 是否可见，控制菜单是否在导航中显示，默认为true
     */
    private Boolean visible = true;
}