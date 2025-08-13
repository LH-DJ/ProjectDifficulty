package com.example.nettycluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Netty集群应用主类
 * 集成Spring Boot和Netty集群功能
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.nettycluster")
public class NettyClusterApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyClusterApplication.class);
    
    public static void main(String[] args) {
        logger.info("启动Netty集群应用...");
        SpringApplication.run(NettyClusterApplication.class, args);
        logger.info("Netty集群应用启动完成");
    }
}
