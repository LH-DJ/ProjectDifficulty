package com.example.nettycluster.config;

import com.example.nettycluster.manager.ClusterManager;
import com.example.nettycluster.server.ClusterServer;
import com.example.nettycluster.discovery.SimpleDiscoveryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置类
 * 管理核心Bean的创建和配置
 */
@Configuration
public class AppConfig {
    
    /**
     * 集群管理器Bean
     */
    @Bean
    public ClusterManager clusterManager(ClusterConfig clusterConfig) {
        return new ClusterManager(clusterConfig);
    }
    
    /**
     * Netty集群服务器Bean
     */
    @Bean
    public ClusterServer clusterServer(ClusterManager clusterManager, ClusterConfig clusterConfig, SimpleDiscoveryService discoveryService) {
        return new ClusterServer(clusterConfig, clusterManager, discoveryService);
    }
}
