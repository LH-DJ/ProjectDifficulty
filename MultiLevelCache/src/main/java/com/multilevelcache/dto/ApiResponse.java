package com.multilevelcache.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * API響應DTO類
 * 
 * @author MultiLevelCache Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private Boolean success;
    private String message;
    private T data;
    private Long responseTimeMs;
    private String cacheLevel;
    private Boolean fromCache;
    private LocalDateTime timestamp;

    /**
     * 成功響應
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("操作成功")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功響應（帶緩存信息）
     */
    public static <T> ApiResponse<T> success(T data, Long responseTimeMs, String cacheLevel, Boolean fromCache) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("操作成功")
                .data(data)
                .responseTimeMs(responseTimeMs)
                .cacheLevel(cacheLevel)
                .fromCache(fromCache)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 失敗響應
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
} 