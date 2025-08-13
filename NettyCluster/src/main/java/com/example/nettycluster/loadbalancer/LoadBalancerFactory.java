package com.example.nettycluster.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 负载均衡器工厂
 */
public class LoadBalancerFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerFactory.class);
    
    private static final Map<String, LoadBalancer> loadBalancers = new HashMap<>();
    
    static {
        // 注册内置的负载均衡器
        registerLoadBalancer("round-robin", new RoundRobinLoadBalancer());
        registerLoadBalancer("random", new RandomLoadBalancer());
        registerLoadBalancer("least-connections", new LeastConnectionsLoadBalancer());
    }
    
    /**
     * 获取负载均衡器
     * 
     * @param strategy 策略名称
     * @return 负载均衡器实例
     */
    public static LoadBalancer getLoadBalancer(String strategy) {
        LoadBalancer loadBalancer = loadBalancers.get(strategy);
        if (loadBalancer == null) {
            logger.warn("Load balancer strategy '{}' not found, using round-robin as default", strategy);
            loadBalancer = loadBalancers.get("round-robin");
        }
        return loadBalancer;
    }
    
    /**
     * 注册负载均衡器
     * 
     * @param name 负载均衡器名称
     * @param loadBalancer 负载均衡器实例
     */
    public static void registerLoadBalancer(String name, LoadBalancer loadBalancer) {
        loadBalancers.put(name, loadBalancer);
        logger.info("Registered load balancer: {}", name);
    }
    
    /**
     * 获取所有可用的负载均衡器名称
     * 
     * @return 负载均衡器名称列表
     */
    public static String[] getAvailableStrategies() {
        return loadBalancers.keySet().toArray(new String[0]);
    }
    
    /**
     * 检查负载均衡器策略是否存在
     * 
     * @param strategy 策略名称
     * @return 是否存在
     */
    public static boolean hasStrategy(String strategy) {
        return loadBalancers.containsKey(strategy);
    }
}
