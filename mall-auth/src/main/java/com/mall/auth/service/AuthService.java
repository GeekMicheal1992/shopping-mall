package com.mall.auth.service;

import com.mall.auth.dto.LoginRequest;
import com.mall.auth.vo.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    void logout(String accessToken);
}
