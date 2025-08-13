package com.example.nettycluster.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot配置属性绑定配置类
 * 启用配置属性绑定功能
 */
@Configuration
@EnableConfigurationProperties(ClusterConfig.class)
public class ConfigurationPropertiesConfig {
}
