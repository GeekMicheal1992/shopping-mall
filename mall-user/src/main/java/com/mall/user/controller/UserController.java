package com.mall.user.controller;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mall.user.dto.CreateUserRequest;
import com.mall.user.entity.User;
import com.mall.user.service.UserService;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
public class UserController {
   private  final UserService userService;  

   public UserController(UserService userService) {
         this.userService = userService;
    }
    
    @GetMapping("/info")
    public User getMethodName(@RequestHeader("X-User-Id") Long userId) {

        return userService.getUserById(userId);
    }
    
    @PutMapping("/info")
    public int updateUserInfo(@RequestHeader("X-User-Id") Long userId, @RequestBody User user) {
        user.setId(userId);
        return userService.updateUser(user);
    }

    @PostMapping("/create")
    public void createUser(@RequestBody CreateUserRequest request) {
        User user = new User();
        user.setId(request.getId());
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userService.createUser(user);
    }
}
