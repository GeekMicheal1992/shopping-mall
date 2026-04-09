package com.mall.common.error;

public enum ErrorCode {
    SUCCESS(0, "success"),
    PARAM_INVALID(1000, "参数错误"),
    SYSTEM_ERROR(1099, "系统异常"),

    UNAUTHORIZED(2000, "未登录"),
    TOKEN_INVALID(2001, "token无效"),
    TOKEN_EXPIRED(2002, "token已过期"),
    FORBIDDEN(2004, "无权限"),
    RATE_LIMITED(2005, "请求过于频繁"); 

    private final int code;
    private final String message;
    
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    private ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
      
}
