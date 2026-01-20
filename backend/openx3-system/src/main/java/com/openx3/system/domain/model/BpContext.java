package com.openx3.system.domain.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务上下文（JWT / Token Payload 的核心部分）
 * 对应文档 bp_context: uid/tid/dept/posts
 */
@Data
public class BpContext implements Serializable {

    /**
     * Employee ID（业务身份 uid）
     */
    private String uid;

    /**
     * Tenant ID（租户 tid）
     */
    private String tid;

    /**
     * Dept ID
     */
    private String deptId;

    /**
     * 当前岗位编码列表
     */
    private List<String> posts = new ArrayList<>();
}

