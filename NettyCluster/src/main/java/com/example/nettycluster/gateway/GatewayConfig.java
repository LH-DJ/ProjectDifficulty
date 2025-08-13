package com.example.nettycluster.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Spring Cloud Gateway配置类
 * 配置路由规则和过滤器
 */
@Configuration
public class GatewayConfig {
    
    /**
     * 配置路由规则
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Netty集群API路由
                .route("netty-cluster-api", r -> r
                        .path("/api/cluster/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "netty-gateway")
                                .addResponseHeader("X-Gateway-Response", "true")
                        )
                        .uri("lb://netty-cluster")
                )
                
                // 集群管理路由
                .route("cluster-management", r -> r
                        .path("/api/management/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Management-API", "true")
                        )
                        .uri("lb://netty-cluster")
                )
                
                // 监控指标路由
                .route("metrics", r -> r
                        .path("/actuator/metrics/**")
                        .filters(f -> f
                                .addRequestHeader("X-Metrics-API", "true")
                        )
                        .uri("lb://netty-cluster")
                )
                
                // 健康检查路由
                .route("health", r -> r
                        .path("/actuator/health/**")
                        .filters(f -> f
                                .addRequestHeader("X-Health-API", "true")
                        )
                        .uri("lb://netty-cluster")
                )
                
                .build();
    }
}
