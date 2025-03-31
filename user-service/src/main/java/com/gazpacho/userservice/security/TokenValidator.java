package com.gazpacho.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class TokenValidator {
    @Value("${JWT_SECRET}")
    private String secretKey;
    private Key key;

    @PostConstruct
    public void init() {
        this.key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Token is invalid or expired
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims != null ? Long.valueOf(claims.getSubject()) : null;
    }

    public String getEmail(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("email", String.class) : null;
    }

    private Claims getClaims(String token) {
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

}
