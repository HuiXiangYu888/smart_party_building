package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分明细实体，对应表 points
 */
@Data
public class Points {
    private Long id;
    private Long userId;
    private Long branchId;
    private Integer points;
    private String description;
    private LocalDateTime createdAt;
}


