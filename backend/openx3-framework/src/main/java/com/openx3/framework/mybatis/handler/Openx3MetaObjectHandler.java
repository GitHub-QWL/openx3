package com.openx3.framework.mybatis.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MyBatis-Plus 审计字段自动填充
 * - createTime/updateTime
 * - createBy/updateBy（优先使用 openx3_uid，其次 openx3_sub）
 */
@Slf4j
@Component
public class Openx3MetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "createBy", String.class, currentOperator());
        strictInsertFill(metaObject, "updateBy", String.class, currentOperator());
        strictInsertFill(metaObject, "tenantId", String.class, currentTenantId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, "updateBy", String.class, currentOperator());
    }

    private String currentOperator() {
        try {
            if (!StpUtil.isLogin()) return "system";
            Object uid = StpUtil.getTokenSession().get("openx3_uid");
            if (uid != null) return Objects.toString(uid);
            Object sub = StpUtil.getTokenSession().get("openx3_sub");
            if (sub != null) return Objects.toString(sub);
            return Objects.toString(StpUtil.getLoginId(), "system");
        } catch (Exception e) {
            return "system";
        }
    }

    private String currentTenantId() {
        try {
            if (!StpUtil.isLogin()) return "000000";
            Object tid = StpUtil.getTokenSession().get("openx3_tid");
            if (tid != null) return Objects.toString(tid);
            return "000000";
        } catch (Exception e) {
            return "000000";
        }
    }
}

