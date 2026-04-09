package com.mall.common.api;

//通用响应类
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private Long timestamp;
    private String requestId;
    public ApiResponse(int code, String message, T data, Long timestamp, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.requestId = requestId;
    }
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    public String getRequestId() {
        return requestId;
    }
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    public ApiResponse() {
    }

   
}