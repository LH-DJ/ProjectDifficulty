package com.example.common.result;

import lombok.Data;

/**
 * 通用響應結果類
 * @param <T> 數據類型
 */
@Data
public class Result<T> {
    
    /**
     * 響應碼
     */
    private Integer code;
    
    /**
     * 響應消息
     */
    private String message;
    
    /**
     * 響應數據
     */
    private T data;
    
    /**
     * 時間戳
     */
    private Long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }
    
    /**
     * 成功響應
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }
    
    /**
     * 成功響應（帶數據）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    /**
     * 成功響應（自定義消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 失敗響應
     */
    public static <T> Result<T> failed() {
        return new Result<>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage());
    }
    
    /**
     * 失敗響應（自定義消息）
     */
    public static <T> Result<T> failed(String message) {
        return new Result<>(ResultCode.FAILED.getCode(), message);
    }
    
    /**
     * 失敗響應（自定義錯誤碼和消息）
     */
    public static <T> Result<T> failed(IErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage());
    }
    
    /**
     * 失敗響應（自定義錯誤碼和消息）
     */
    public static <T> Result<T> failed(Integer code, String message) {
        return new Result<>(code, message);
    }
    
    /**
     * 參數驗證失敗
     */
    public static <T> Result<T> validateFailed() {
        return failed(ResultCode.VALIDATE_FAILED);
    }
    
    /**
     * 參數驗證失敗（自定義消息）
     */
    public static <T> Result<T> validateFailed(String message) {
        return new Result<>(ResultCode.VALIDATE_FAILED.getCode(), message);
    }
    
    /**
     * 未登錄
     */
    public static <T> Result<T> unauthorized(T data) {
        return new Result<>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), data);
    }
    
    /**
     * 未授權
     */
    public static <T> Result<T> forbidden(T data) {
        return new Result<>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), data);
    }
}
