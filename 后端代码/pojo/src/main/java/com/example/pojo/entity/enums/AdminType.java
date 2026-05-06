package com.example.pojo.entity.enums;

/**
 * 管理员类型枚举
 */
public enum AdminType {
    
    /**
     * 支部管理员
     */
    BRANCH_ADMIN("BRANCH_ADMIN", "支部管理员"),
    
    /**
     * 系统管理员
     */
    SYSTEM_ADMIN("SYSTEM_ADMIN", "系统管理员");
    
    private final String code;
    private final String description;
    
    AdminType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static AdminType fromCode(String code) {
        for (AdminType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown admin type code: " + code);
    }
}
