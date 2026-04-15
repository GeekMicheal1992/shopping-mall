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
        return userMapper.insert(user);
    }
    @Override
    public int updateUser(User user) {
       User existingUser = userMapper.selectById(user.getId());
       
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if(user.getAvatar() != null){
            existingUser.setAvatar(user.getAvatar());
        }
        existingUser.setUpdatedAt(LocalDateTime.now());
        return userMapper.update(existingUser);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
        throw new RuntimeException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateAvatar(userId, avatarUrl);
    }       
    
}   
