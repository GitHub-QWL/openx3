package com.openx3.framework.mybatis.datascope;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 数据权限拦截器（MyBatis-Plus）
 */
@Slf4j
public class DataScopeInnerInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor,
                            MappedStatement ms,
                            Object parameter,
                            RowBounds rowBounds,
                            ResultHandler resultHandler,
                            BoundSql boundSql) {
        DataScope dataScope = findDataScope(ms);
        if (dataScope == null) return;

        // 必须登录才应用数据权限
        if (!StpUtil.isLogin()) return;

        String dsScope = Objects.toString(StpUtil.getTokenSession().get("openx3_ds_scope"), "");
        String deptId = Objects.toString(StpUtil.getTokenSession().get("openx3_dept_id"), "");
        String uid = Objects.toString(StpUtil.getTokenSession().get("openx3_uid"), "");
        String tid = Objects.toString(StpUtil.getTokenSession().get("openx3_tid"), "");

        if (!StringUtils.hasText(dsScope)) dsScope = "SELF";

        String tenantCond = buildTenantCondition(dataScope, tid);
        String dataCond = buildDataCondition(dataScope, dsScope, deptId, uid);
        String cond = join(tenantCond, dataCond);
        if (!StringUtils.hasText(cond)) return;

        try {
            String originalSql = boundSql.getSql();
            Statement stmt = CCJSqlParserUtil.parse(originalSql);
            if (!(stmt instanceof Select select)) return;
            if (!(select.getSelectBody() instanceof PlainSelect ps)) return;

            Expression append = CCJSqlParserUtil.parseExpression(cond);
            if (ps.getWhere() == null) {
                ps.setWhere(append);
            } else {
                ps.setWhere(new AndExpression(ps.getWhere(), append));
            }

            String newSql = select.toString();
            setBoundSql(boundSql, newSql);
        } catch (Exception e) {
            // 解析失败时不应阻塞主流程，但要打日志方便排查
            log.warn("DataScope 拦截失败，跳过本次过滤: {}", e.getMessage());
        }
    }

    private void setBoundSql(BoundSql boundSql, String sql) {
        try {
            Field f = BoundSql.class.getDeclaredField("sql");
            f.setAccessible(true);
            f.set(boundSql, sql);
        } catch (Exception e) {
            log.warn("DataScope 写回 SQL 失败: {}", e.getMessage());
        }
    }

    private DataScope findDataScope(MappedStatement ms) {
        String id = ms.getId();
        int idx = id.lastIndexOf('.');
        if (idx <= 0) return null;

        String className = id.substring(0, idx);
        String methodName = id.substring(idx + 1);
        try {
            Class<?> mapperClass = Class.forName(className);
            for (Method m : mapperClass.getMethods()) {
                if (m.getName().equals(methodName) && m.isAnnotationPresent(DataScope.class)) {
                    return m.getAnnotation(DataScope.class);
                }
            }
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    private String buildTenantCondition(DataScope ds, String tid) {
        if (!ds.tenant()) return null;
        if (!text(tid)) return null;
        String alias = resolveAlias(ds);
        if (!text(alias)) return null;
        return alias + "." + ds.tenantField() + " = '" + escape(tid) + "'";
    }

    private String buildDataCondition(DataScope ds, String scope, String deptId, String uid) {
        String alias = resolveAlias(ds);
        if (!StringUtils.hasText(alias)) return null;

        String deptField = ds.deptField();
        String userField = ds.userField();

        String s = scope.toUpperCase(Locale.ROOT);
        return switch (s) {
            case "ALL" -> null;
            case "DEPT" -> text(deptId) ? alias + "." + deptField + " = '" + escape(deptId) + "'" : null;
            case "DEPT_AND_CHILD" -> text(deptId)
                    ? alias + "." + deptField + " IN (SELECT id FROM sys_dept WHERE (',' || tree_path || ',') LIKE '%," + escapeLike(deptId) + ",%')"
                    : null;
            case "CUSTOM" -> buildCustomDeptCondition(alias, deptField);
            case "SELF" -> text(uid) ? alias + "." + userField + " = '" + escape(uid) + "'" : null;
            default -> text(uid) ? alias + "." + userField + " = '" + escape(uid) + "'" : null;
        };
    }

    private String join(String a, String b) {
        if (!text(a)) return b;
        if (!text(b)) return a;
        return "(" + a + ") AND (" + b + ")";
    }

    private String resolveAlias(DataScope ds) {
        if (ds == null) return null;
        if (text(ds.alias())) return ds.alias();
        if (text(ds.value())) return ds.value();
        return null;
    }

    private String buildCustomDeptCondition(String alias, String deptField) {
        Object obj = StpUtil.getTokenSession().get("openx3_custom_dept_ids");
        if (obj instanceof List<?> list && !list.isEmpty()) {
            String in = list.stream()
                    .filter(Objects::nonNull)
                    .map(x -> "'" + escape(String.valueOf(x)) + "'")
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            return in.isEmpty() ? null : alias + "." + deptField + " IN (" + in + ")";
        }
        if (obj instanceof String s && text(s)) {
            String[] parts = s.split(",");
            String in = "";
            for (String p : parts) {
                if (!text(p)) continue;
                in += (in.isEmpty() ? "" : ",") + "'" + escape(p.trim()) + "'";
            }
            return in.isEmpty() ? null : alias + "." + deptField + " IN (" + in + ")";
        }
        return null;
    }

    private boolean text(String s) {
        return StringUtils.hasText(s);
    }

    private String escape(String s) {
        return s.replace("'", "''");
    }

    private String escapeLike(String s) {
        // LIKE 模式下，转义单引号即可；id 本身不应包含 %/_
        return escape(s);
    }
}

