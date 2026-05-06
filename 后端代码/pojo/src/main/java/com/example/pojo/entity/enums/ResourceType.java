package com.example.pojo.entity.enums;

/**
 * 学习资源类型枚举（只支持视频）
 */
public enum ResourceType {
    
    /**
     * 视频
     */
    VIDEO("VIDEO", "视频");
    
    private final String code;
    private final String description;
    
    ResourceType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ResourceType fromCode(String code) {
        for (ResourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown resource type code: " + code);
    }
}
