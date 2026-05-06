package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemSetting {
    private Long id;
    private String settingKey;
    private String settingValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


