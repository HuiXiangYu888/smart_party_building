package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习资源实体类
 * 对应数据库表：study_resources
 */
@Data
public class StudyResource {
    private Long id;
    private String title;
    /** VIDEO - 只支持视频类型 */
    private String type;
    /** OSS URL */
    private String url;
    /** 封面图URL */
    private String coverUrl;
    /** 上传管理员ID（admins.id） */
    private Long createdBy;
    private LocalDateTime createdAt;
}
