package com.example.pojo.entity.enums;

/**
 * 通用状态枚举
 */
public enum CommonStatus {
    
    /**
     * 待处理
     */
    PENDING("PENDING", "待处理"),
    
    /**
     * 已通过
     */
    APPROVED("APPROVED", "已通过"),
    
    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝");
    
    private final String code;
    private final String description;
    
    CommonStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static CommonStatus fromCode(String code) {
        for (CommonStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
