package com.openx3.core.support;

import com.openx3.common.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通用数据访问对象 (增强版)
 * 核心能力：
 * 1. 动态 SQL 执行 (Query)
 * 2. 动态对象保存 (Insert/Update)
 * 3. 动态对象删除 (Delete)
 * 4. 字段名自动映射 (Camel <-> Underline)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * 驼峰转换 Mapper (user_name -> userName)
     * 用于查询结果集的自动映射
     */
    private static class CamelCaseRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            // 使用 LinkedCaseInsensitiveMap 保证顺序且 Key 不区分大小写
            Map<String, Object> map = new LinkedCaseInsensitiveMap<>();
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String column = metaData.getColumnLabel(i);
                map.put(toCamelCase(column), rs.getObject(i));
            }
            return map;
        }

        // 下划线转驼峰 (user_name -> userName)
        private String toCamelCase(String s) {
            if (s == null) return null;
            StringBuilder sb = new StringBuilder();
            boolean upper = false;
            for (char c : s.toLowerCase().toCharArray()) {
                if (c == '_') {
                    upper = true;
                } else {
                    sb.append(upper ? Character.toUpperCase(c) : c);
                    upper = false;
                }
            }
            return sb.toString();
        }
    }

    private final CamelCaseRowMapper rowMapper = new CamelCaseRowMapper();

    // ========================================================================
    //  查询接口 (Read)
    // ========================================================================

    /**
     * 通用查询列表
     * @return List<Map<String, Object>> (Key 为驼峰格式)
     */
    public List<Map<String, Object>> findList(String sql, Map<String, Object> params) {
        return jdbcTemplate.query(sql, params != null ? params : Collections.emptyMap(), rowMapper);
    }

    /**
     * 通用查询单条
     * @return Map<String, Object> (Key 为驼峰格式)
     */
    public Map<String, Object> findOne(String sql, Map<String, Object> params) {
        try {
            return jdbcTemplate.queryForObject(sql, params != null ? params : Collections.emptyMap(), rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ========================================================================
    //  写入接口 (Write) - [新增]
    // ========================================================================

    /**
     * 通用保存 (自动判断 Insert 或 Update)
     * 规则：如果 data 中包含 'id' 且不为空，则 Update；否则 Insert
     *
     * @param tableName 数据库表名
     * @param data      数据 Map (Key 建议为驼峰，会自动转下划线)
     * @return 影响行数
     */
    public int save(String tableName, Map<String, Object> data) {
        if (data == null || data.isEmpty()) return 0;

        // 1. 检查 ID 状态
        String id = (String) data.get("id");
        // 兼容空字符串的情况
        boolean isInsert = !StringUtils.hasText(id);

        StringBuilder sql = new StringBuilder();

        if (isInsert) {
            // --- 插入逻辑 ---
            // 自动生成 ID (优先使用 IdGenerator 雪花算法，如果未引入则使用 UUID)
            try {
                // 尝试使用雪花算法 (有序 ID，性能最佳)
                id = String.valueOf(IdGenerator.nextId());
            } catch (NoClassDefFoundError | Exception e) {
                // 降级为 UUID
                id = UUID.randomUUID().toString();
            }
            // 将生成的 ID 放入 data，以便写入数据库
            data.put("id", id);

            sql.append("INSERT INTO ").append(tableName).append(" (");
            StringBuilder values = new StringBuilder(") VALUES (");

            int i = 0;
            for (String key : data.keySet()) {
                if (i++ > 0) {
                    sql.append(", ");
                    values.append(", ");
                }
                // Key 转为数据库字段名 (userName -> user_name)
                sql.append(camelToUnderline(key));
                // Value 使用命名参数 (:userName)
                values.append(":").append(key);
            }
            sql.append(values).append(")");

        } else {
            // --- 更新逻辑 ---
            sql.append("UPDATE ").append(tableName).append(" SET ");
            int i = 0;
            for (String key : data.keySet()) {
                // ID 不作为更新字段
                if ("id".equals(key)) continue;

                if (i++ > 0) sql.append(", ");
                sql.append(camelToUnderline(key)).append(" = :").append(key);
            }
            sql.append(" WHERE id = :id");
        }

        if (log.isDebugEnabled()) {
            log.debug("GenericDao Execute SQL: {}", sql);
            log.debug("GenericDao Params: {}", data);
        }

        return jdbcTemplate.update(sql.toString(), data);
    }

    /**
     * 通用删除
     * @param tableName 表名
     * @param id 主键 ID
     */
    public int delete(String tableName, String id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = :id";
        return jdbcTemplate.update(sql, Collections.singletonMap("id", id));
    }

    /**
     * 通用 SQL 执行 (如复杂的 Update/Delete 语句)
     */
    public int update(String sql, Map<String, Object> params) {
        return jdbcTemplate.update(sql, params != null ? params : Collections.emptyMap());
    }

    // ========================================================================
    //  辅助工具
    // ========================================================================

    /**
     * 驼峰转下划线 (userName -> user_name)
     * 用于生成 SQL 时将 Map Key 转换为列名
     */
    private String camelToUnderline(String param) {
        if (param == null || param.trim().isEmpty()) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}