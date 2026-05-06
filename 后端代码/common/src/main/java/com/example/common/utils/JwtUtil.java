package com.example.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和解析JWT令牌
 */
@Component
public class JwtUtil {

    /**
     * JWT密钥
     */
    private static final String SECRET_KEY_STRING = "mySecretKey123456789012345678901234567890";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    
    /**
     * JWT过期时间（毫秒）- 2小时
     */
    private static final long EXPIRATION_TIME = 2 * 60 * 60 * 1000;
    
    /**
     * 刷新令牌过期时间（毫秒）- 7天
     */
    private static final long REFRESH_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成JWT令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param userType 用户类型（MEMBER/ADMIN）
     * @return JWT令牌
     */
    public String generateToken(Long userId, String username, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("userType", userType);
        claims.put("authType", "MEMBER".equals(userType) ? "USER" : "ADMIN");
        claims.put("created", new Date());
        
        return createToken(claims, EXPIRATION_TIME);
    }

    /**
     * 生成刷新令牌
     *
     * @param userId 用户ID
     * @return 刷新令牌
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        claims.put("created", new Date());
        
        return createToken(claims, REFRESH_EXPIRATION_TIME);
    }

    /**
     * 创建JWT令牌
     *
     * @param claims        声明信息
     * @param expirationTime 过期时间
     * @return JWT令牌
     */
    private String createToken(Map<String, Object> claims, long expirationTime) {
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从令牌中获取用户类型
     *
     * @param token JWT令牌
     * @return 用户类型
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userType", String.class);
    }

    /**
     * 从令牌中获取认证类型
     *
     * @param token JWT令牌
     * @return 认证类型（USER/ADMIN）
     */
    public String getAuthTypeFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("authType", String.class);
    }

    /**
     * 从令牌中获取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 检查令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证令牌
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中获取所有声明信息
     *
     * @param token JWT令牌
     * @return 声明信息
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Authorization头中提取令牌
     *
     * @param authorizationHeader Authorization头
     * @return JWT令牌
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}