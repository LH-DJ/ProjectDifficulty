package com.example.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具類
 */
public class DateUtil {
    
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    
    /**
     * 格式化當前時間
     */
    public static String formatNow() {
        return format(LocalDateTime.now(), DEFAULT_PATTERN);
    }
    
    /**
     * 格式化日期時間
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
    
    /**
     * 格式化日期時間（使用默認格式）
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_PATTERN);
    }
    
    /**
     * 解析日期時間字符串
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
    
    /**
     * 解析日期時間字符串（使用默認格式）
     */
    public static LocalDateTime parse(String dateTimeStr) {
        return parse(dateTimeStr, DEFAULT_PATTERN);
    }
    
    /**
     * 獲取當前時間戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 獲取當前日期字符串
     */
    public static String getCurrentDate() {
        return format(LocalDateTime.now(), DATE_PATTERN);
    }
    
    /**
     * 獲取當前時間字符串
     */
    public static String getCurrentTime() {
        return format(LocalDateTime.now(), TIME_PATTERN);
    }
}
