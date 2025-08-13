package com.example.nettycluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 简化的测试应用类
 * 用于验证基本功能，避免复杂的依赖
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.nettycluster")
public class SimpleTestApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleTestApplication.class);
    
    public static void main(String[] args) {
        logger.info("启动简化的Netty集群测试应用...");
        
        try {
            SpringApplication.run(SimpleTestApplication.class, args);
            logger.info("简化应用启动成功！");
            
            // 保持应用运行
            Thread.currentThread().join();
            
        } catch (Exception e) {
            logger.error("应用启动失败", e);
            System.exit(1);
        }
    }
}
