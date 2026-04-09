package com.mall.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn; // token过期时间，单位秒
    private String userId;
    private String username;
}
