package com.mall.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.mall.user.entity.User;

@Mapper
public interface UserMapper {
    User selectById(@Param("id") Long id);
    User selectByUsername(@Param("username") String username);
    int insert(User user);
    int update(User user);
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);   
}