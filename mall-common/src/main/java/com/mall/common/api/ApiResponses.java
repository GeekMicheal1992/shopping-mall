package com.mall.common.api;

import java.util.List;


public final class ApiResponses{

        private static final int SUCCESS_CODE = 0;
        private static final String SUCCESS_MESSAGE = "success";
        public ApiResponses() {
        }
        
        public static ApiResponse<Void> success() {
                ApiResponse<Void> response = new ApiResponse<>();
                response.setCode(SUCCESS_CODE);
                response.setMessage(SUCCESS_MESSAGE);
                response.setData(null);
                response.setTimestamp(System.currentTimeMillis());
                response.setRequestId(null);
                return response;
        }

        public static <T> ApiResponse<T> success(T data) {
                ApiResponse<T> response = new ApiResponse<>();
                response.setCode(SUCCESS_CODE);
                response.setMessage(SUCCESS_MESSAGE);
                response.setData(data);
                response.setTimestamp(System.currentTimeMillis());
                response.setRequestId(null);
                return response;
        }

        public static ApiResponse<Void> fail(int code, String message) {
        ApiResponse<Void> resp = new ApiResponse<>();
        resp.setCode(code);
        resp.setMessage(message);
        resp.setData(null);
        resp.setTimestamp(System.currentTimeMillis());
        resp.setRequestId(null);
        return resp;
    }

     public static <T> ApiResponse<T> fail(int code, String message, T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setCode(code);
        resp.setMessage(message);
        resp.setData(data);
        resp.setTimestamp(System.currentTimeMillis());
        resp.setRequestId(null);
        return resp;
    }
    
    public static <T> ApiResponse<PageData<T>> page(List<T> list, long total, int page, int size) {
        PageData<T> pageData = new PageData<>(page, size, total, list);
        return success(pageData);
    }

}
