package com.example.pojo.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理端登录DTO
 */
@Data
public class AdminLoginDTO {
    
    /**
     * 用户名/手机号
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 验证码（可选）
     */
    private String captcha;
    
    /**
     * 验证码标识（可选）
     */
    private String captchaId;
}
