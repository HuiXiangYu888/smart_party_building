package com.example.server.interceptor;

import com.example.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.server.service.UserService;
import com.example.pojo.entity.Admin;

/**
 * JWT拦截器
 * 用于验证请求中的JWT令牌
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 所有业务接口均需鉴权（除全局放行的登录、刷新、登出等在 WebConfig 中配置）
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        // 获取Authorization头
        String authorizationHeader = request.getHeader("Authorization");
        
        if (!StringUtils.hasText(authorizationHeader)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 401, \"message\": \"缺少Authorization头\"}");
            return false;
        }

        // 提取JWT令牌
        String token = jwtUtil.extractTokenFromHeader(authorizationHeader);
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 401, \"message\": \"无效的Authorization头格式\"}");
            return false;
        }

        try {
            // 验证令牌
            if (!jwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\": 401, \"message\": \"令牌已过期或无效\"}");
                return false;
            }

            // 将用户信息存储到请求属性中，供后续使用
            request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
            request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
            String userType = jwtUtil.getUserTypeFromToken(token);
            request.setAttribute("userType", userType);
            request.setAttribute("authType", jwtUtil.getAuthTypeFromToken(token));

            // 二次校验管理员状态，禁用后阻断所有访问
            if ("SYSTEM_ADMIN".equals(userType) || "BRANCH_ADMIN".equals(userType)) {
                Long uid = (Long) request.getAttribute("userId");
                Admin admin = userService.findAdminById(uid);
                if (admin == null || (admin.getStatus() != null && !"ACTIVE".equalsIgnoreCase(admin.getStatus()))) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\": 401, \"message\": \"账号已禁用或不存在\"}");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 401, \"message\": \"令牌验证失败\"}");
            return false;
        }
    }
}
