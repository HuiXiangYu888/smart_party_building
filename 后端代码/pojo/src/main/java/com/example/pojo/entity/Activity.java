package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动实体类
 * 对应数据库表：activities
 */
@Data
public class Activity {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 活动标题
     */
    private String title;
    
    /**
     * 活动描述
     */
    private String description;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 所属支部ID
     */
    private Long branchId;
    
    /**
     * 创建人ID（指向admins.id）
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
