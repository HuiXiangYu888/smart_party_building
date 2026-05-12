package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告实体类
 * 对应数据库表：announcements
 */
@Data
public class Announcement {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告内容
     */
    private String content;
    
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
    
    /**
     * 支部名称（关联查询时使用）
     */
    private String branchName;
    
    /**
     * 创建人姓名（关联查询时使用）
     */
    private String createdByName;
    
    /**
     * 最后修改时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 最后修改人ID
     */
    private Long lastModifiedBy;
    
    /**
     * 最后修改人姓名（关联查询时使用）
     */
    private String lastModifiedByName;
}