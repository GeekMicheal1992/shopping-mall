package com.mall.order.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private Long timestamp;
    private String requestId;
}