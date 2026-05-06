package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分明细实体类
 * 对应数据库表：points
 */
@Data
public class Point {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID（指向members.id）
     */
    private Long userId;
    
    /**
     * 所属支部ID
     */
    private Long branchId;
    
    /**
     * 积分变化值
     */
    private Integer points;
    
    /**
     * 积分说明
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
