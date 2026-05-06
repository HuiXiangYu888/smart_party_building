package com.example.pojo.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户类型
     */
    private String userType;
    
    /**
     * 令牌过期时间（毫秒）
     */
    private Long expiresIn;
}
