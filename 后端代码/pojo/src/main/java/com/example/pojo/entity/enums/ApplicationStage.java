package com.example.pojo.entity.enums;

/**
 * 审核阶段枚举
 */
public enum ApplicationStage {
    
    /**
     * 积极分子
     */
    POSITIVE_MEMBER("POSITIVE_MEMBER", "积极分子"),
    
    /**
     * 入党申请
     */
    PARTY_APPLICATION("PARTY_APPLICATION", "入党申请"),
    
    /**
     * 预备党员
     */
    PREPARE_MEMBER("PREPARE_MEMBER", "预备党员");
    
    private final String code;
    private final String description;
    
    ApplicationStage(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ApplicationStage fromCode(String code) {
        for (ApplicationStage stage : values()) {
            if (stage.code.equals(code)) {
                return stage;
            }
        }
        throw new IllegalArgumentException("Unknown application stage code: " + code);
    }
}
