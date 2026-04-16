package com.mall.auth.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mall.auth.dto.ChangePasswordRequest;
import com.mall.auth.dto.ForgotPasswordRequest;
import com.mall.auth.dto.LoginRequest;
import com.mall.auth.dto.LogoutRequest;
import com.mall.auth.dto.RefreshTokenRequest;
import com.mall.auth.dto.RegisterRequest;
import com.mall.auth.dto.ResetPasswordRequest;
import com.mall.auth.service.AuthService;
import com.mall.auth.vo.LoginResponse;
import com.mall.common.api.ApiResponse;
import com.mall.common.api.ApiResponses;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


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
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ApiResponses.success();
    }

    @PostMapping("/password/change")
    public  ApiResponse<Void> changePassword( @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ApiResponses.success();
    }
   
    @PostMapping("/password/forgot")
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.sendResetCode(request.getPhone());
        return ApiResponses.success();
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponses.success();
    }
    
}