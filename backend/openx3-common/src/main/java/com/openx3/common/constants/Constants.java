package com.openx3.common.constants;

/**
 * 系统常量类
 */
public class Constants {
    
    /**
     * 逻辑删除标志
     */
    public static final Integer DEL_FLAG_NORMAL = 0;  // 正常
    public static final Integer DEL_FLAG_DELETED = 1; // 已删除
    
    /**
     * 默认租户ID
     */
    public static final String DEFAULT_TENANT_ID = "000000";
    
    /**
     * 脚本事件类型
     */
    public static final String SCRIPT_EVENT_SAVE = "SAVE";
    public static final String SCRIPT_EVENT_DELETE = "DELETE";
    public static final String SCRIPT_EVENT_QUERY = "QUERY";
    public static final String SCRIPT_EVENT_POST_LOAD = "POST_LOAD";
    
    /**
     * 窗口类型
     */
    public static final String WINDOW_TYPE_LIST = "LIST";
    public static final String WINDOW_TYPE_FORM = "FORM";
    public static final String WINDOW_TYPE_MODAL = "MODAL";
    
    /**
     * 字段类型
     */
    public static final String FIELD_TYPE_STRING = "STRING";
    public static final String FIELD_TYPE_NUMBER = "NUMBER";
    public static final String FIELD_TYPE_DATE = "DATE";
    public static final String FIELD_TYPE_BOOLEAN = "BOOLEAN";
    public static final String FIELD_TYPE_ARRAY = "ARRAY";
    
    /**
     * 权限码前缀
     */
    public static final String AUTH_PREFIX = "AUTH_";
    
    /**
     * JSONB 扩展字段列名
     */
    public static final String EXT_DATA_COLUMN = "ext_data";
    
    /**
     * 主键字段名
     */
    public static final String PK_FIELD_DEFAULT = "id";
}
