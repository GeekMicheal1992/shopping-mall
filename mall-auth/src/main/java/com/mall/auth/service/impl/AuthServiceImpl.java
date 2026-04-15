package com.mall.auth.service.impl;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.mall.auth.dto.ChangePasswordRequest;
import com.mall.auth.dto.CreateUserRequest;
import com.mall.auth.dto.LoginRequest;
import com.mall.auth.dto.RegisterRequest;
import com.mall.auth.entity.AuthUser;
import com.mall.auth.mapper.AuthUserMapper;
import com.mall.auth.service.AuthService;
import com.mall.auth.service.JwtService;
import com.mall.auth.vo.LoginResponse;
import com.mall.common.error.BizException;
import com.mall.common.error.ErrorCode;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


@Service
public class AuthServiceImpl implements AuthService {

    private final AuthUserMapper authUserMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    public AuthServiceImpl(AuthUserMapper authUserMapper, JwtService jwtService, StringRedisTemplate stringRedisTemplate, RestTemplate restTemplate) {
        this.authUserMapper = authUserMapper;
        this.jwtService = jwtService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.restTemplate = restTemplate;
    }
  
    @Override
    public LoginResponse login(LoginRequest request) {

        if(request== null 
                || request.getUsername() == null
                || request.getPassword() == null
                || request.getUsername().trim().isEmpty()
                || request.getPassword().trim().isEmpty()) {
                throw new BizException(ErrorCode.PARAM_INVALID);
            }
            String username = request.getUsername();
            String password = request.getPassword();
            AuthUser user = authUserMapper.selectByUsername(username);
            if (user == null) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }
            if (!user.getUsername().equals(username)) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }
            if (!StringUtils.hasText(user.getPasswordHash())) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }
            boolean passwordMatched = false;
            try {
                passwordMatched = passwordEncoder.matches(password, user.getPasswordHash());
            } catch (IllegalArgumentException ex) {
                passwordMatched = false;
            }
            if (!passwordMatched) {
                // Dev fallback: allows plain-text password in DB for quick local verification.
                passwordMatched = password.equals(user.getPasswordHash());
            }
            if (!passwordMatched) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }
            user.setLastLoginAt(LocalDateTime.now());
            authUserMapper.updateLastLoginAt(user.getId(), user.getLastLoginAt());
            LoginResponse response = new LoginResponse();
            response.setToken(jwtService.generateAccessToken(user.getId(), user.getUsername()));
            response.setRefreshToken(jwtService.generateRefreshToken(user.getId(), user.getUsername()));
            response.setExpiresIn(jwtService.getRemainingSeconds(response.getToken()));
            response.setUserId(user.getId().toString());
            response.setUsername(user.getUsername());
            response.setTokenType("Bearer");
            
        return response;
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }

         if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
            
        }
        Long userId = jwtService.getUserId(refreshToken);
        String username = jwtService.getUsername(refreshToken);
        if(userId == null || username == null) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }
        AuthUser user = authUserMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        LoginResponse response = new LoginResponse();
        response.setToken(jwtService.generateAccessToken(user.getId(), user.getUsername()));
        response.setRefreshToken(jwtService.generateRefreshToken(user.getId(), user.getUsername()));
        response.setExpiresIn(jwtService.getRemainingSeconds(response.getToken()));
        response.setUserId(user.getId().toString());
        response.setUsername(user.getUsername());
        response.setTokenType("Bearer");
        return response;
    }

    @Override
    public void logout(String accessToken) {
        if(accessToken == null || accessToken.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (!jwtService.validateAccessToken(accessToken)) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }

        String jti = jwtService.getJti(accessToken);
        long ttlSeconds = jwtService.getRemainingSeconds(accessToken);
        if (jti == null || jti.trim().isEmpty()) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }
        if (ttlSeconds > 0) {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX  + jti;
            stringRedisTemplate.opsForValue().set(blacklistKey, "1", ttlSeconds, TimeUnit.SECONDS);
        }     

    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        
         if(request== null 
                || request.getUsername() == null
                || request.getPassword() == null
                || request.getUsername().trim().isEmpty()
                || request.getPassword().trim().isEmpty()) {
                throw new BizException(ErrorCode.PARAM_INVALID);
            }
            String username = request.getUsername().trim();
            String password = request.getPassword();
            AuthUser existingUser = authUserMapper.selectByUsername(username);
            if (existingUser != null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "Username already exists");
            }
            AuthUser newUser = new AuthUser();
            newUser.setUsername(username);
            newUser.setPasswordHash(passwordEncoder.encode(password));
            newUser.setNickname(request.getNickname());
            newUser.setPhone(request.getPhone());
            newUser.setEmail(request.getEmail());
            newUser.setStatus(1);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());
            authUserMapper.insert(newUser);
            try {
                CreateUserRequest createUserRequest = new CreateUserRequest();
                createUserRequest.setId(newUser.getId());
                createUserRequest.setUsername(newUser.getUsername());
                createUserRequest.setNickname(newUser.getNickname());
                createUserRequest.setPhone(newUser.getPhone());
                createUserRequest.setEmail(newUser.getEmail());
                String userServiceUrl = "http://localhost:8082/create";
            restTemplate.postForObject(userServiceUrl, createUserRequest, String.class);
            } catch (Exception  e) {
                log.error("Failed to create user record in user-service: {}", e.getMessage());
            }
           
            LoginResponse response = new LoginResponse();
            response.setToken(jwtService.generateAccessToken(newUser.getId(), newUser.getUsername()));
            response.setRefreshToken(jwtService.generateRefreshToken(newUser.getId(), newUser.getUsername()));
            response.setExpiresIn(jwtService.getRemainingSeconds(response.getToken()));
            response.setUserId(newUser.getId().toString());
            response.setUsername(newUser.getUsername());
            response.setTokenType("Bearer");
            return response;
    }

    @Override
    public void changePassword(Long userId, @Valid ChangePasswordRequest request) {
        AuthUser user = authUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
       String oldPassword = request.getOldPassword();
       boolean passwordMatched = false;
         try {
              passwordMatched = passwordEncoder.matches(oldPassword, user.getPasswordHash());}
            catch (IllegalArgumentException ex) {
                passwordMatched = false;
            }
        if (!passwordMatched) {
            throw new BizException(ErrorCode.PARAM_INVALID, "旧密码不正确");
        }
            String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
            authUserMapper.updatePassword(userId, newPasswordHash);
       
    }
}


