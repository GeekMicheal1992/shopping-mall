package com.mall.common.security;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class TokenValidator {
    private static final String SECRET_KEY = "your-256-bit-secret"; 

    public static Claims parseToken(String token) {
        try {
               return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
            } catch (Exception e) {
                return null;
            }
        }
      
    public static boolean validateToken(String token) {
        
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        if(claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
            return false;
        }
        String tokenType = claims.get("type", String.class);
        return "access".equals(tokenType);
    }
}

