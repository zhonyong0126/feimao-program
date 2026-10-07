package com.feimao.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author feimao
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:604800000}")
    private Long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET未配置或少于32字节");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成token
     */
    public String generateToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        return createToken(claims);
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userId", Long.class);
        }
        return null;
    }

    /**
     * 获取用户类型
     */
    public String getUserType(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userType", String.class);
        }
        return null;
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return !claims.getExpiration().before(new Date());
    }

}
