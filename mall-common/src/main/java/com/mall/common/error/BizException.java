package com.mall.common.error;

public class BizException extends RuntimeException{
    private int code;
    private String message;
   
    public BizException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
     public BizException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.message = customMessage;
    }

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

}
