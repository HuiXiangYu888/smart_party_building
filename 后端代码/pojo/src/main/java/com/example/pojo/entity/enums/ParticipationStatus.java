package com.example.pojo.entity.enums;

/**
 * 活动参与状态枚举
 */
public enum ParticipationStatus {
    
    /**
     * 已参与
     */
    PARTICIPATED("PARTICIPATED", "已参与"),
    
    /**
     * 未参与
     */
    NOT_PARTICIPATED("NOT_PARTICIPATED", "未参与");
    
    private final String code;
    private final String description;
    
    ParticipationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ParticipationStatus fromCode(String code) {
        for (ParticipationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown participation status code: " + code);
    }
}
