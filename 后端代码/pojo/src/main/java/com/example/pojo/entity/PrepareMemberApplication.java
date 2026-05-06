package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预备党员审核表实体类
 * 对应数据库表：prepare_member_application
 */
@Data
public class PrepareMemberApplication {

    /** 主键ID */
    private Long id;

    /** 用户ID（指向members.id） */
    private Long userId;

    /** 考察报告（可为纯文本或JSON字符串，见后端兼容逻辑） */
    private String evaluationReport;

    /** 预备期开始时间 */
    private LocalDateTime probationPeriodStart;

    /** 预备期结束时间 */
    private LocalDateTime probationPeriodEnd;

    /** 审核状态：PENDING/APPROVED/REJECTED */
    private String status;

    /** 审核人ID（指向admins.id） */
    private Long reviewerId;

    /** 审核时间 */
    private LocalDateTime reviewedAt;

    /** 审核意见 */
    private String comments;
}
