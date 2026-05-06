package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积极分子审核表实体类
 * 对应数据库表：positive_member_application
 */
@Data
public class PositiveMemberApplication {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID（指向members.id）
     */
    private Long userId;
    
    /**
     * 申请详情
     */
    private String applicationDetails;
    
    /**
     * 相关证明材料（存JSON字符串）
     */
    private String supportingDocuments;
    
    /**
     * 审核状态
     * PENDING: 待审核
     * APPROVED: 已通过
     * REJECTED: 已拒绝
     */
    private String status;
    
    /**
     * 审核人ID（指向admins.id）
     */
    private Long reviewerId;
    
    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;
    
    /**
     * 审核意见
     */
    private String comments;
}
