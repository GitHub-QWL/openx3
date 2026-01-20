package com.openx3.system.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树 VO（用于前端动态菜单/路由渲染）
 */
@Data
public class MenuVO {

    private String id;
    private String parentId;
    private String title;
    private String path;
    private String component;
    private String icon;
    private Integer type;
    private Integer sortNo;
    private Boolean visible;
    private String perms;

    private List<MenuVO> children = new ArrayList<>();
}

