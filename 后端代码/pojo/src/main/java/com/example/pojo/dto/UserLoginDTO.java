package com.example.pojo.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户端登录DTO（支持用户名登录，用户名可以是学号或身份证号）
 */
@Data
public class UserLoginDTO {
    
    /**
     * 用户名（学号或身份证号）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
