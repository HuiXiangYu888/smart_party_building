package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MemberUpdateRequest {
    private Long id;
    private Long memberId;
    private String fieldsJson; // JSON of proposed changes
    private String status; // PENDING, APPROVED, REJECTED
    private Long reviewerId;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}


