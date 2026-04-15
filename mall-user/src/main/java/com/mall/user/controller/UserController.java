package com.mall.user.controller;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mall.user.dto.CreateUserRequest;
import com.mall.user.entity.User;
import com.mall.user.service.UserService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.imageio.ImageIO;

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
        
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 限制文件大小
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("文件不能超过2MB");
        }
        
        // 读取文件内容
        byte[] bytes = file.getBytes();
        
        // 1. 通过魔数验证（快速过滤）
        if (!isImageByMagicNumber(bytes)) {
            throw new RuntimeException("文件格式不正确，只支持图片");
        }
        
        // 2. 通过ImageIO验证（更严格）
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            if (ImageIO.read(bais) == null) {
                throw new RuntimeException("文件内容不是有效的图片");
            }
        }
        
        // 3. 验证扩展名（辅助）
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!extension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
                throw new RuntimeException("不支持的文件扩展名");
            }
        }
        
        // 保存文件
        String filename = UUID.randomUUID().toString() + extension;
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        Path filePath = uploadDir.resolve(filename);
        Files.write(filePath, bytes);
        
        // 更新用户头像
        String avatarUrl = urlPrefix + filename;
        userService.updateAvatar(userId, avatarUrl);
        
        return avatarUrl;
    }

// 魔数验证方法
    private boolean isImageByMagicNumber(byte[] bytes) {
        if (bytes == null || bytes.length < 8) {
            return false;
        }
        
        // JPEG
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG
        if ((bytes[0] & 0xFF) == 0x89 && (bytes[1] & 0xFF) == 0x50 && 
            (bytes[2] & 0xFF) == 0x4E && (bytes[3] & 0xFF) == 0x47) {
            return true;
        }
        // GIF
        if ((bytes[0] & 0xFF) == 0x47 && (bytes[1] & 0xFF) == 0x49 && 
            (bytes[2] & 0xFF) == 0x46 && (bytes[3] & 0xFF) == 0x38) {
            return true;
        }
        // BMP
        if ((bytes[0] & 0xFF) == 0x42 && (bytes[1] & 0xFF) == 0x4D) {
            return true;
        }
        return false;
    }
  
}
