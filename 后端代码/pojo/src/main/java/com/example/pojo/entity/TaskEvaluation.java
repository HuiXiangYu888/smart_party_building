package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务评价实体类
 * 对应数据库表：task_evaluations
 */
@Data
public class TaskEvaluation {
    /** 主键ID */
    private Long id;

    /** 任务ID */
    private Long taskId;

    /** 评价人（members.id） */
    private Long userId;

    /** 评分 1-5 */
    private Integer rating;

    /** 评价内容 */
    private String comment;

    /** 创建时间 */
    private LocalDateTime createdAt;
}


