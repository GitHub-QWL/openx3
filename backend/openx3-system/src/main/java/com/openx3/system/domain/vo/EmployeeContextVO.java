package com.openx3.system.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 员工可选业务上下文（用于登录后选择身份）
 */
@Data
public class EmployeeContextVO {

    private String employeeId;
    private String realName;
    private Boolean isMain;

    private String tenantId;
    private String tenantName;

    private String deptId;
    private String deptName;

    private List<String> postCodes = new ArrayList<>();
    private List<String> postNames = new ArrayList<>();
}

