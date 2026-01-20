package com.openx3.system.security.field;

import java.lang.annotation.*;

/**
 * 指定字段权限策略的资源编码（resource_code）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldPolicyResource {

    String value();
}

