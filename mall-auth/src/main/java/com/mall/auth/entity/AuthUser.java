package com.mall.auth.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private Integer status; // 1=enabled, 0=disabled
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
