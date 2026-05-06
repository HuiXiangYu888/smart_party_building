package com.example.server.service;

import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.AdminLoginDTO;
import com.example.pojo.dto.RefreshTokenDTO;
import com.example.pojo.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户端登录（支持用户名登录，用户名可以是学号或身份证号）
     *
     * @param userLoginDTO 用户登录请求DTO
     * @return 登录响应VO
     */
    LoginVO userLogin(UserLoginDTO userLoginDTO);
    
    /**
     * 管理端登录
     *
     * @param adminLoginDTO 管理员登录请求DTO
     * @return 登录响应VO
     */
    LoginVO adminLogin(AdminLoginDTO adminLoginDTO);
    
    /**
     * 刷新访问令牌
     *
     * @param refreshTokenDTO 刷新令牌DTO
     * @return 新的访问令牌
     */
    String refreshToken(RefreshTokenDTO refreshTokenDTO);
    
    /**
     * 用户登出
     *
     * @param userId 用户ID
     */
    void logout(Long userId);
    
    /**
     * 验证令牌有效性
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
}
