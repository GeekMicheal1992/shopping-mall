package com.mall.user.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.mall.user.entity.User;

public interface UserService {
    User getUserById(Long id);
    User getUserByUsername(String username);
    int createUser(User user);
    int updateUser(User user);
    void updateAvatar(Long userId, String avatarUrl);
    String uploadAvatar(Long userId, MultipartFile file) throws IOException;  
}
