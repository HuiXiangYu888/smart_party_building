package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动参与记录实体类
 * 对应数据库表：activity_participants
 */
@Data
public class ActivityParticipant {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 活动ID
     */
    private Long activityId;
    
    /**
     * 用户ID（指向members.id）
     */
    private Long userId;
    
    /**
     * 参与状态
     * PARTICIPATED: 已参与
     * NOT_PARTICIPATED: 未参与
     */
    private String status;
    
    /**
     * 参与时间
     */
    private LocalDateTime joinedAt;
}
