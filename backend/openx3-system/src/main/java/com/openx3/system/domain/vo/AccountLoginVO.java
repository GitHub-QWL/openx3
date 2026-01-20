package com.openx3.system.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号登录返回（不签发 Token，仅返回可选员工列表）
 */
@Data
public class AccountLoginVO {

    private String accountId;

    private List<EmployeeContextVO> employees = new ArrayList<>();
}

