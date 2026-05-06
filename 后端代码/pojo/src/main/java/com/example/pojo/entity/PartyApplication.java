package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 入党申请表实体类
 * 对应数据库表：party_application
 */
@Data
public class PartyApplication {

    /** 主键ID */
    private Long id;

    /** 用户ID（指向members.id） */
    private Long userId;

    /** 申请详情 */
    private String applicationDetails;

    /** 培训记录或附件JSON（后端已兼容存放附件） */
    private String trainingRecords;

    /** 审核状态：PENDING/APPROVED/REJECTED */
    private String status;

    /** 审核人ID（指向admins.id） */
    private Long reviewerId;

    /** 审核时间 */
    private LocalDateTime reviewedAt;

    /** 审核意见 */
    private String comments;
}
