package com.mall.common.security;

import java.util.Date;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenValidator {
    
    private String secretKey;

    @Value("${jwt.secret}")
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            System.err.println("Token parse error: " + e.getMessage());
            return null;
        }
    }
    
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
            return false;
        }
        String tokenType = claims.get("type", String.class);
        return "access".equals(tokenType);
    }
}