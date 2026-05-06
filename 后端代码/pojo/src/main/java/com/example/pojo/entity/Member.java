package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 普通用户实体类
 * 对应数据库表：members
 */
@Data
public class Member {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 学号（可选）
     */
    private String studentId;
    
    /**
     * 身份证号
     */
    private String idNumber;
    
    /**
     * 登录密码（MD5加密存储）
     */
    private String password;
    
    /**
     * 姓名
     */
    private String username;
    
    /**
     * 手机号
     */
    private String mobile;
    
    /**
     * 所属支部
     */
    private Long branchId;

    /**
     * 政治面貌
     * NON_PARTY_MEMBER: 非党员
     * POSITIVE_MEMBER: 积极分子
     * PREPARE_MEMBER: 预备党员
     * PARTY_MEMBER: 正式党员
     */
    private String politicalStatus;
    /** 入党时间（可为空） */
    private LocalDateTime joinDate;
    /** 审核状态：PENDING(未审核)/APPROVED/REJECTED */
    private String reviewStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
