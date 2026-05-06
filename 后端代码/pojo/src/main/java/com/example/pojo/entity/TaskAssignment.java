package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务分配实体类
 * 对应数据库表：task_assignments
 */
@Data
public class TaskAssignment {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 分配用户ID（指向members.id）
     */
    private Long userId;
    
    /**
     * 任务状态
     * PENDING: 待处理
     * IN_PROGRESS: 进行中
     * COMPLETED: 已完成
     */
    private String status;
    
    /**
     * 分配时间
     */
    private LocalDateTime assignedAt;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}
