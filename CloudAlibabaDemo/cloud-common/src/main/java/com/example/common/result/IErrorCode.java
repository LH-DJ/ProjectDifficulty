package com.example.common.result;

/**
 * 錯誤碼接口
 */
public interface IErrorCode {
    
    /**
     * 獲取錯誤碼
     */
    Integer getCode();
    
    /**
     * 獲取錯誤消息
     */
    String getMessage();
}
