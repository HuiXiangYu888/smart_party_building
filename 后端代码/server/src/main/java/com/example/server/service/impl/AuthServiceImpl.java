package com.example.server.service.impl;

import com.example.common.utils.JwtUtil;
import com.example.common.utils.PasswordUtil;
import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.AdminLoginDTO;
import com.example.pojo.dto.RefreshTokenDTO;
import com.example.pojo.entity.Member;
import com.example.pojo.entity.Admin;
import com.example.pojo.vo.LoginVO;
import com.example.server.service.AuthService;
import com.example.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    @Override
    public LoginVO userLogin(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        
        // 先尝试按学号查找用户
        Member member = userService.findMemberByStudentId(username.trim());
        
        // 如果没找到，再尝试按身份证号查找
        if (member == null) {
            member = userService.findMemberByIdNumber(username.trim());
        }
        
        if (member == null) {
            throw new RuntimeException("用户不存在，请先注册");
        }
        
        // 验证密码
        if (!passwordUtil.matches(password, member.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 生成用户端令牌
        return createUserLoginVO(member.getId(), member.getUsername(), "MEMBER");
    }
    
    @Override
    public LoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();
        
        // 查找管理员
        Admin admin = userService.findAdminByUsername(username);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        // 校验账号是否启用
        if (admin.getStatus() != null && !"ACTIVE".equalsIgnoreCase(admin.getStatus())) {
            throw new RuntimeException("账号已被禁用，请联系超级管理员");
        }
        
        // 验证密码
        if (!passwordUtil.matches(password, admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 生成管理端令牌
        return createAdminLoginVO(admin.getId(), admin.getUsername(), admin.getAdminType());
    }
    
    @Override
    public String refreshToken(RefreshTokenDTO refreshTokenDTO) {
        String refreshToken = refreshTokenDTO.getRefreshToken();
        
        // 验证刷新令牌
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("刷新令牌无效或已过期");
        }
        
        // 从刷新令牌中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        
        // 生成新的访问令牌
        String userType = getUserTypeById(userId);
        String username = getUsernameById(userId, userType);
        
        return jwtUtil.generateToken(userId, username, userType);
    }
    
    @Override
    public void logout(Long userId) {
        // TODO: 实际项目中可以将令牌加入黑名单或清除缓存
        // 这里只是示例实现
    }
    
    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    /**
     * 创建用户端登录响应VO
     */
    private LoginVO createUserLoginVO(Long userId, String username, String userType) {
        LoginVO loginVO = new LoginVO();
        
        // 生成用户端访问令牌和刷新令牌
        String accessToken = jwtUtil.generateToken(userId, username, userType);
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setUserId(userId);
        loginVO.setUsername(username);
        loginVO.setUserType(userType);
        loginVO.setExpiresIn(2 * 60 * 60 * 1000L); // 2小时
        
        return loginVO;
    }
    
    /**
     * 创建管理端登录响应VO
     */
    private LoginVO createAdminLoginVO(Long userId, String username, String userType) {
        LoginVO loginVO = new LoginVO();
        
        // 生成管理端访问令牌和刷新令牌
        String accessToken = jwtUtil.generateToken(userId, username, userType);
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setUserId(userId);
        loginVO.setUsername(username);
        loginVO.setUserType(userType);
        loginVO.setExpiresIn(2 * 60 * 60 * 1000L); // 2小时
        
        return loginVO;
    }
    
    /**
     * 创建登录响应VO（兼容旧版本）
     */
    private LoginVO createLoginVO(Long userId, String username, String userType) {
        if ("MEMBER".equals(userType)) {
            return createUserLoginVO(userId, username, userType);
        } else {
            return createAdminLoginVO(userId, username, userType);
        }
    }
    
    /**
     * 根据用户ID获取用户类型
     */
    private String getUserTypeById(Long userId) {
        // 先尝试查找普通用户
        Member member = userService.findMemberById(userId);
        if (member != null) {
            return "MEMBER";
        }
        // 查找管理员，返回具体管理员类型（SYSTEM_ADMIN/BRANCH_ADMIN）
        Admin admin = userService.findAdminById(userId);
        if (admin != null) {
            return admin.getAdminType();
        }
        throw new RuntimeException("用户不存在");
    }
    
    /**
     * 根据用户ID和类型获取用户名
     */
    private String getUsernameById(Long userId, String userType) {
        if ("MEMBER".equals(userType)) {
            Member member = userService.findMemberById(userId);
            return member != null ? member.getUsername() : null;
        } else if ("SYSTEM_ADMIN".equals(userType) || "BRANCH_ADMIN".equals(userType)) {
            Admin admin = userService.findAdminById(userId);
            return admin != null ? admin.getUsername() : null;
        }
        return null;
    }
}
