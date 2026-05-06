package com.example.pojo.entity.enums;

/**
 * 政治面貌枚举
 */
public enum PoliticalStatus {
    
    /**
     * 非党员
     */
    NON_PARTY_MEMBER("NON_PARTY_MEMBER", "非党员"),
    
    /**
     * 积极分子
     */
    POSITIVE_MEMBER("POSITIVE_MEMBER", "积极分子"),
    
    /**
     * 预备党员
     */
    PREPARE_MEMBER("PREPARE_MEMBER", "预备党员"),
    
    /**
     * 正式党员
     */
    PARTY_MEMBER("PARTY_MEMBER", "正式党员");
    
    private final String code;
    private final String description;
    
    PoliticalStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static PoliticalStatus fromCode(String code) {
        for (PoliticalStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown political status code: " + code);
    }
}
