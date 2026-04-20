package com.mall.auth.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.mall.auth.config.JwtProperties;
import com.mall.auth.service.JwtService;
import com.mall.common.error.BizException;
import com.mall.common.error.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {

    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    
    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessExpireMs = jwtProperties.getExpiration();
        this.refreshExpireMs = jwtProperties.getRefreshExpiration();
    }

    private final SecretKey signingKey;
    private final long accessExpireMs;
    private final long refreshExpireMs;


    @Override
    public String generateAccessToken(Long userId, String username,String role)  {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpireMs);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
            .id(jti)                          
            .subject(username)                
            .claim(CLAIM_UID, userId)          
            .claim(CLAIM_TYPE, TYPE_ACCESS)    
            .claim("role", role)              
            .issuedAt(now)                    
            .expiration(expiry)                
            .signWith(signingKey)             
            .compact();
    }

    @Override
    public String generateRefreshToken(Long userId, String username,String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpireMs);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(username)
                .claim(CLAIM_UID, userId)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public boolean validateAccessToken(String token) {
       
        return validateByType(token, TYPE_ACCESS);
    }

    @Override
    public boolean validateRefreshToken(String token) {
       return validateByType(token, TYPE_REFRESH);
       
    }

    @Override
    public Long getUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
        throw new BizException(ErrorCode.TOKEN_INVALID);
            
        }   
        try{

            Claims claims = parseClaims(token);
            Object uid = claims.get("uid");
            
            if(uid == null) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
            }
        
            if (uid instanceof Number number) {
                return number.longValue();
            }

            return Long.parseLong(uid.toString());
        }catch (Exception e) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }
    }


    @Override
    public String getUsername(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    @Override
    public String getJti(String token) {
        Claims claims = parseClaims(token);
        return claims.getId();
    }

    @Override
    public long getRemainingSeconds(String token) {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();
            return Math.max(0, (expiration.getTime() - now) / 1000);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    }

    @Override
    public boolean validateByType(String token, String expectedType) {
          if (token == null || token.trim().isEmpty()) {
           return false;
          }
          try {
              Claims claims = parseClaims(token);
              String type = claims.get(CLAIM_TYPE, String.class);
              return expectedType.equals(type);
          } catch (Exception e) {
              return false;
          }
    }

    
    
}