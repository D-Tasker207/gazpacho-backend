package com.gazpacho.userservice.security;

import com.gazpacho.userservice.security.TokenUtils.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class TokenValidator {
    @Value("${JWT_ACCESS_SECRET}")
    private String accessSecret;
    private Key accessKey;

    @Value("${JWT_REFRESH_SECRET}")
    private String refreshSecret;
    private Key refreshKey;

    @PostConstruct
    public void init() {
        this.accessKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(accessSecret.getBytes());
        this.refreshKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(refreshSecret.getBytes());
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, TokenType.ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, TokenType.REFRESH);
    }

    public Long getUserIdFromAccessToken(String token) {
        return getUserId(token, TokenType.ACCESS);
    }

    public Long getUserIdFromRefreshToken(String token) {
        return getUserId(token, TokenType.REFRESH);
    }

    private boolean validateToken(String token, TokenType type) {
        Claims claims = getClaims(token, type);
        return claims != null && type.name().equalsIgnoreCase(claims.get("type", String.class));
    }

    private Long getUserId(String token, TokenType type) {
        Claims claims = getClaims(token, type);
        return claims != null ? Long.valueOf(claims.getSubject()) : null;
    }

    private Claims getClaims(String token, TokenType type) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(type == TokenType.ACCESS ? accessKey : refreshKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
}
