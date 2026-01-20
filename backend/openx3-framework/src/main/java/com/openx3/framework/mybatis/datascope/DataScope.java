package com.openx3.framework.mybatis.datascope;

import java.lang.annotation.*;

/**
 * 数据权限注解（行级过滤）
 * 用法：标注在 Mapper 方法上，拦截器会根据当前 Token 的 ds_scope 自动拼接 WHERE 条件。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * SQL 表别名（强烈建议填写）
     */
    String alias() default "";

    /**
     * alias 的简写写法：@DataScope("t")
     */
    String value() default "";

    /**
     * 是否启用租户隔离条件（tenant_id = tid）
     */
    boolean tenant() default true;

    /**
     * 租户字段名（默认 tenant_id）
     */
    String tenantField() default "tenant_id";

    /**
     * 部门字段名（默认 dept_id）
     */
    String deptField() default "dept_id";

    /**
     * 用户字段名（默认 create_by）
     */
    String userField() default "create_by";
}

