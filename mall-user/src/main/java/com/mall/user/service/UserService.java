package com.mall.user.service;

import com.mall.user.entity.User;

public interface UserService {
    User getUserById(Long id);
    User getUserByUsername(String username);
    int createUser(User user);
    int updateUser(User user);
}
