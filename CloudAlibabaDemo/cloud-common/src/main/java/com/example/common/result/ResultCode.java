package com.example.common.result;

/**
 * 結果碼枚舉
 */
public enum ResultCode implements IErrorCode {
    
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失敗"),
    VALIDATE_FAILED(404, "參數檢驗失敗"),
    UNAUTHORIZED(401, "暫未登錄或token已經過期"),
    FORBIDDEN(403, "沒有相關權限"),
    
    // 用戶相關錯誤碼
    USER_NOT_EXIST(1001, "用戶不存在"),
    USERNAME_OR_PASSWORD_ERROR(1002, "用戶名或密碼錯誤"),
    USER_ALREADY_EXIST(1003, "用戶已存在"),
    
    // 商品相關錯誤碼
    PRODUCT_NOT_EXIST(2001, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(2002, "商品庫存不足"),
    PRODUCT_ALREADY_EXISTS(2003, "商品名稱已存在"),
    INVALID_STOCK_AMOUNT(2004, "無效的庫存數量"),
    INSUFFICIENT_STOCK(2005, "庫存不足"),
    
    // 訂單相關錯誤碼
    ORDER_NOT_EXIST(3001, "訂單不存在"),
    ORDER_STATUS_ERROR(3002, "訂單狀態錯誤"),
    
    // 系統相關錯誤碼
    SYSTEM_ERROR(9001, "系統錯誤"),
    NETWORK_ERROR(9002, "網絡錯誤"),
    DATABASE_ERROR(9003, "數據庫錯誤");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    @Override
    public Integer getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
