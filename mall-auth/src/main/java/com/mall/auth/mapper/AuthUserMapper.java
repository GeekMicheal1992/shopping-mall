package com.mall.auth.mapper;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mall.auth.entity.AuthUser;

@Mapper
public interface AuthUserMapper {

	AuthUser selectByUsername(@Param("username") String username);

    int insert(AuthUser newUser);

	int updateLastLoginAt(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);

	AuthUser selectById(@Param("id") Long id);
}
