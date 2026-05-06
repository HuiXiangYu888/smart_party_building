package com.example.server.controller;

import com.example.common.result.Result;
import com.example.common.utils.JwtUtil;
import com.example.pojo.dto.AdminLoginDTO;
import com.example.pojo.dto.RefreshTokenDTO;
import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.vo.LoginVO;
import com.example.server.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户端登录（微信小程序）
     *
     * @param userLoginDTO 用户登录请求
     * @return 登录结果
     */
    @PostMapping("/user/login")
    public Result<LoginVO> userLogin(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        LoginVO loginVO = authService.userLogin(userLoginDTO);
        return Result.success(loginVO);
    }

    /**
     * 管理端登录（HTML页面）
     *
     * @param adminLoginDTO 管理员登录请求
     * @return 登录结果
     */
    @PostMapping("/admin/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody AdminLoginDTO adminLoginDTO) {
        LoginVO loginVO = authService.adminLogin(adminLoginDTO);
        return Result.success(loginVO);
    }

    /**
     * 刷新令牌
     *
     * @param refreshTokenDTO 刷新令牌请求
     * @return 新的访问令牌
     */
    @PostMapping("/refresh")
    public Result<String> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        String newToken = authService.refreshToken(refreshTokenDTO);
        return Result.success(newToken);
    }

    /**
     * 登出
     *
     * @param request HTTP请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        try {
            // 尝试从请求头中获取token（可选）
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                try {
                    // 如果token有效，获取用户ID并调用服务层登出
                    if (jwtUtil.validateToken(token)) {
                        Long userId = jwtUtil.getUserIdFromToken(token);
                        authService.logout(userId);
                    }
                } catch (Exception e) {
                    // token无效或过期，忽略错误，继续执行登出流程
                    System.out.println("Token验证失败，继续执行登出: " + e.getMessage());
                }
            }
            
            // 无论token是否有效，都返回成功
            return Result.success("登出成功");
        } catch (Exception e) {
            // 即使出现异常，也返回成功，确保客户端能正常清理本地状态
            return Result.success("登出成功");
        }
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @param authType 认证类型
     * @return 用户信息
     */
    @GetMapping("/profile")
    public Result<Object> getProfile(
            @RequestAttribute Long userId,
            @RequestAttribute String username,
            @RequestAttribute String userType,
            @RequestAttribute String authType) {
        
        return Result.success(new Object() {
            public final Long id = userId;
            public final String name = username;
            public final String type = userType;
            public final String auth = authType;
        });
    }
}
