package com.mall.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {
     @NotBlank(message = "accessToken不能为空")
    private String accessToken;
}
