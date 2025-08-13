package com.example.nettycluster.health;

import com.example.nettycluster.manager.ClusterManager;
import com.example.nettycluster.model.ClusterNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 简化的集群健康检查指示器
 * 不依赖Spring Boot Actuator，直接提供REST端点
 */
@Component
@RestController
@RequestMapping("/health")
public class SimpleHealthIndicator {
    
    @Autowired
    private ClusterManager clusterManager;
    
    /**
     * 健康检查端点
     */
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            if (!clusterManager.isRunning()) {
                health.put("status", "DOWN");
                health.put("error", "集群管理器未运行");
                return health;
            }
            
            int totalNodes = clusterManager.getNodeCount();
            int onlineNodes = clusterManager.getOnlineNodes().size();
            int offlineNodes = clusterManager.getOfflineNodes().size();
            
            // 检查集群健康状态
            if (totalNodes == 0) {
                health.put("status", "UNKNOWN");
                health.put("message", "集群中暂无节点");
                health.put("totalNodes", totalNodes);
                return health;
            }
            
            // 计算健康度
            double healthRatio = (double) onlineNodes / totalNodes;
            String status;
            
            if (healthRatio >= 0.8) {
                status = "UP";
            } else if (healthRatio >= 0.5) {
                status = "UNKNOWN";
            } else {
                status = "DOWN";
            }
            
            // 添加详细信息
            health.put("status", status);
            health.put("totalNodes", totalNodes);
            health.put("onlineNodes", onlineNodes);
            health.put("offlineNodes", offlineNodes);
            health.put("healthRatio", String.format("%.2f", healthRatio));
            health.put("localNodeId", clusterManager.getLocalNodeId());
            
            // 添加节点状态信息
            List<ClusterNode> allNodes = clusterManager.getAllNodes();
            Map<String, String> nodeStatuses = allNodes.stream()
                    .collect(Collectors.toMap(
                            ClusterNode::getNodeId,
                            node -> node.getStatus().toString()
                    ));
            health.put("nodeStatuses", nodeStatuses);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", "健康检查异常: " + e.getMessage());
            health.put("exception", e.getClass().getSimpleName());
        }
        
        return health;
    }
    
    /**
     * 详细健康检查端点
     */
    @GetMapping("/detailed")
    public Map<String, Object> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            health.put("timestamp", System.currentTimeMillis());
            health.put("application", "netty-cluster");
            health.put("version", "1.0.0");
            
            // 基础健康信息
            Map<String, Object> basicHealth = health();
            health.putAll(basicHealth);
            
            // 添加额外信息
            health.put("clusterManager", clusterManager.isRunning() ? "RUNNING" : "STOPPED");
            health.put("uptime", System.currentTimeMillis() - clusterManager.getConfig().getClass().hashCode());
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", "详细健康检查异常: " + e.getMessage());
        }
        
        return health;
    }
}
