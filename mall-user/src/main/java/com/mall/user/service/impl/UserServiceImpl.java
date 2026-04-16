package com.mall.user.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mall.common.error.BizException;
import com.mall.common.error.ErrorCode;
import com.mall.user.entity.User;
import com.mall.user.mapper.UserMapper;
import com.mall.user.service.UserService;

@Service
public class UserServiceImpl implements UserService     {

    private final UserMapper userMapper;
    @Value("${avatar.upload-path:./uploads/avatars/}")
    private String uploadPath;

    @Value("${avatar.url-prefix:/avatars/}")
    private String urlPrefix;
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

    @Override
public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
    
    // 1. 验证文件
    if (file.isEmpty()) {
        throw new BizException(ErrorCode.PARAM_INVALID, "文件不能为空");
    }
    
    // 2. 限制文件大小
    if (file.getSize() > 2 * 1024 * 1024) {
        throw new BizException(ErrorCode.PARAM_INVALID, "文件不能超过2MB");
    }
    
    // 3. 读取文件内容
    byte[] bytes = file.getBytes();
    
    // 4. 魔数验证
    if (!isImageByMagicNumber(bytes)) {
        throw new BizException(ErrorCode.PARAM_INVALID, "文件格式不正确，只支持图片");
    }
    
    // 5. ImageIO 验证
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
        if (ImageIO.read(bais) == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件内容不是有效的图片");
        }
    }
    
    // 6. 验证扩展名
    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!extension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "不支持的文件扩展名");
        }
    }
    
    // 7. 保存文件
    String filename = UUID.randomUUID().toString() + extension;
    Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
    if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
    }
    
    Path filePath = uploadDir.resolve(filename);
    Files.write(filePath, bytes);
    
    // 8. 更新用户头像
    String avatarUrl = urlPrefix + filename;
    updateAvatar(userId, avatarUrl);
    
    return avatarUrl;
}

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
