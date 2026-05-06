package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 民主评议实体类
 * 对应数据库表：democratic_evaluations
 */
@Data
public class DemocraticEvaluation {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 被评议用户ID（指向members.id）
     */
    private Long evaluatedUserId;
    
    /**
     * 评议人ID（指向admins.id）
     */
    private Long evaluatorId;
    
    /**
     * 评分（0-100）
     */
    private Integer score;
    
    /**
     * 评语
     */
    private String comment;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
