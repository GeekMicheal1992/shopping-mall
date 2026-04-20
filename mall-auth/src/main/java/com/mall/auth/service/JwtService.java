package com.mall.auth.service;

public interface JwtService {
    String generateAccessToken(Long userId, String username,String role);
    String generateRefreshToken(Long userId, String username,String role);

    boolean validateAccessToken(String token);
    boolean validateRefreshToken(String token);

    Long getUserId(String token);
    String getUsername(String token);
    String getJti(String token);

    long getRemainingSeconds(String token);

    public boolean validateByType(String token, String expectedType);
  
}
