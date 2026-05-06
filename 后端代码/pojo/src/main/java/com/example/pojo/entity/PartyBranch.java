package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 党支部实体类
 * 对应数据库表：party_branches
 */
@Data
public class PartyBranch {
    /** 主键ID */
    private Long id;

    /** 支部名称 */
    private String name;

    /** 负责人ID（指向admins.id） */
    private Long leaderId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
