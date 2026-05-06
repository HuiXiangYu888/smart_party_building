package com.example.pojo.entity.enums;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    
    /**
     * 待处理
     */
    PENDING("PENDING", "待处理"),
    
    /**
     * 进行中
     */
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    
    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成");
    
    private final String code;
    private final String description;
    
    TaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TaskStatus fromCode(String code) {
        for (TaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown task status code: " + code);
    }
}
