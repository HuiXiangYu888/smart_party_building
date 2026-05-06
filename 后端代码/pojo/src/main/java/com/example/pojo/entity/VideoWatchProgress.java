package com.example.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 视频观看进度实体类
 * 对应数据库表：video_watch_progress
 */
@Data
public class VideoWatchProgress {
    
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
     * 当前观看时间(秒)
     */
    private Integer currentTime;
    
    /**
     * 视频总时长(秒)
     */
    private Integer totalDuration;
    
    /**
     * 观看进度百分比
     */
    private BigDecimal watchPercentage;
    
    /**
     * 最后观看时间
     */
    private LocalDateTime lastWatchTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
