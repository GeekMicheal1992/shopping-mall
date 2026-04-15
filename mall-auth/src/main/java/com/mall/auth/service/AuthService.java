package com.mall.auth.service;

import com.mall.auth.dto.ChangePasswordRequest;
import com.mall.auth.dto.LoginRequest;
import com.mall.auth.dto.RegisterRequest;
import com.mall.auth.vo.LoginResponse;

import jakarta.validation.Valid;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    void logout(String accessToken);
    LoginResponse register(RegisterRequest request);
    void changePassword(Long userId, @Valid ChangePasswordRequest request);
}
