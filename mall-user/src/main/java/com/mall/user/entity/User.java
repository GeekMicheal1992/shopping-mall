package com.mall.user.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}