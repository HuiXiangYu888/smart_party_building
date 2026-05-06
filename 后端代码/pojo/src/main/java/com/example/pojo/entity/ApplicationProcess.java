package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审核流程主表实体类
 * 对应数据库表：application_process
 */
@Data
public class ApplicationProcess {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID（指向members.id）
     */
    private Long userId;
    
    /**
     * 当前审核阶段
     * POSITIVE_MEMBER: 积极分子
     * PARTY_APPLICATION: 入党申请
     * PREPARE_MEMBER: 预备党员
     */
    private String currentStage;
    
    /**
     * 审核状态
     * PENDING: 待审核
     * APPROVED: 已通过
     * REJECTED: 已拒绝
     */
    private String status;
    
    /**
     * 当前审核人ID（指向admins.id）
     */
    private Long reviewerId;
    
    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;
    
    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;
}
