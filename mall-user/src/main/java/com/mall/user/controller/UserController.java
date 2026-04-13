package com.mall.user.controller;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mall.user.entity.User;
import com.mall.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
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
}
