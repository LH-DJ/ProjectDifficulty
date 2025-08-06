package com.multilevelcache;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 多級緩存應用主類
 */
@SpringBootApplication
@MapperScan(basePackages = "com.multilevelcache.mapper")
public class MultiLevelCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiLevelCacheApplication.class, args);
        System.out.println("🚀 多級緩存應用啟動成功！");
        System.out.println("📊 訪問地址: http://localhost:8080");
        System.out.println("🏥 健康檢查: http://localhost:8080/actuator/health");
    }
} 