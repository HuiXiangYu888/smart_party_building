package com.example.server.controller;

import com.example.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 用于验证JWT拦截器是否正常工作
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    /**
     * 测试需要认证的接口
     * 需要在请求头中添加：Authorization: Bearer {token}
     */
    @GetMapping("/protected")
    public Result<Map<String, Object>> testProtected(HttpServletRequest request) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", request.getAttribute("userId"));
        userInfo.put("username", request.getAttribute("username"));
        userInfo.put("userType", request.getAttribute("userType"));
        userInfo.put("message", "这是一个受保护的接口，需要JWT认证");
        
        return Result.success("访问成功", userInfo);
    }
    
    /**
     * 测试公开接口
     */
    @GetMapping("/public")
    public Result<String> testPublic() {
        return Result.success("这是一个公开接口，无需认证");
    }
}
