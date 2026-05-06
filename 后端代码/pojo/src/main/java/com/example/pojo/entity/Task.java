package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务实体类
 * 对应数据库表：tasks
 */
@Data
public class Task {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 任务标题
     */
    private String title;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 截止时间
     */
    private LocalDateTime dueDate;
    
    /**
     * 任务状态：PUBLISHING(发布中)/PENDING_END(待完结)/ENDED(已完结)
     */
    private String status;
    
    /**
     * 完成该任务可获得的积分
     */
    private Integer points;

    /**
     * 活动人数上限
     */
    private Integer capacity;
    
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
