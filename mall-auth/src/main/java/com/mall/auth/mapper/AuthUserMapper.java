package com.mall.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mall.auth.entity.AuthUser;

@Mapper
public interface AuthUserMapper {

	AuthUser selectByUsername(@Param("username") String username);
}
