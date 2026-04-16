package com.mall.user.controller;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.mall.user.dto.CreateUserRequest;
import com.mall.user.entity.User;
import com.mall.user.service.UserService;
import java.io.IOException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
public class UserController {
   private  final UserService userService;  
private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Value("${avatar.upload-path:./uploads/avatars/}")
    private String uploadPath;

    @Value("${avatar.url-prefix:/avatars/}")
    private String urlPrefix;

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

  @PostMapping("/avatar")
public String uploadAvatar(@RequestHeader("X-User-Id") Long userId, 
                           @RequestParam("file") MultipartFile file) throws IOException {
    String avatarUrl = userService.uploadAvatar(userId, file);
    return avatarUrl;
}
  
}
