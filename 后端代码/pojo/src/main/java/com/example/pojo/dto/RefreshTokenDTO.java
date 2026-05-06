package com.example.pojo.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新令牌DTO
 */
@Data
public class RefreshTokenDTO {
    
    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
