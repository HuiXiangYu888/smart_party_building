package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员实体类
 * 对应数据库表：admins
 */
@Data
public class Admin {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 微信OpenID
     */
    private String openId;
    
    /**
     * 姓名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String mobile;
    
    /**
     * 管理员类型
     * BRANCH_ADMIN: 支部管理员
     * SYSTEM_ADMIN: 系统管理员
     */
    private String adminType;
    
    /**
     * 密码（加密存储）
     */
    private String password;
    
    /**
     * 所属支部ID（仅对BRANCH_ADMIN有效）
     */
    private Long branchId;

    /**
     * 所属支部名称（非持久化，查询拼装）
     */
    private String branchName;

    /**
     * 账号状态：ACTIVE/INACTIVE
     */
    private String status;

    /**
     * 最近一次修改人ID（admins.id）
     */
    private Long lastModifiedBy;

    /**
     * 最近一次修改时间
     */
    private LocalDateTime lastModifiedAt;

    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
