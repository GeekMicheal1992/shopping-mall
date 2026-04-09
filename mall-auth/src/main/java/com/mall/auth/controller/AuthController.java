package com.mall.auth.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mall.auth.dto.LoginRequest;
import com.mall.auth.dto.LogoutRequest;
import com.mall.auth.dto.RefreshTokenRequest;
import com.mall.auth.service.AuthService;
import com.mall.auth.vo.LoginResponse;
import com.mall.common.api.ApiResponse;
import com.mall.common.api.ApiResponses;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> postMethodName(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponses.success(response);
    }
    
    @PostMapping("refresh")
    public ApiResponse<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ApiResponses.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody @Valid LogoutRequest request) {
        authService.logout(request.getAccessToken());
        return ApiResponses.success();
    }
}