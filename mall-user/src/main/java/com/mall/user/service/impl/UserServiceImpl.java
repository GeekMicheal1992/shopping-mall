package com.mall.user.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mall.user.entity.User;
import com.mall.user.mapper.UserMapper;
import com.mall.user.service.UserService;

@Service
public class UserServiceImpl implements UserService     {

    private final UserMapper userMapper;
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);   
    }

    @Override
        public int createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setStatus(1);
        return userMapper.insert(user);
    }
    @Override
    public int updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.update(user);
    }       
    
    
}   
