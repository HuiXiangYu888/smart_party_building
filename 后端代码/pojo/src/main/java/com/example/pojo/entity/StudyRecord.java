package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习记录实体类
 * 对应数据库表：study_records
 */
@Data
public class StudyRecord {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID（指向members.id）
     */
    private Long userId;
    
    /**
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 学习时长(分钟)
     */
    private Integer duration;
    
    /**
     * 开始时间
     */
    private LocalDateTime startedAt;
    
    /**
     * 结束时间
     */
    private LocalDateTime endedAt;
}
