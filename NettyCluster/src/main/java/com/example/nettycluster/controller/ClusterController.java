package com.example.nettycluster.controller;

import com.example.nettycluster.manager.ClusterManager;
import com.example.nettycluster.model.ClusterMessage;
import com.example.nettycluster.model.ClusterNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 集群管理REST控制器
 * 提供集群状态查询和管理接口
 */
@RestController
@RequestMapping("/api/cluster")
@CrossOrigin(origins = "*")
public class ClusterController {
    
    @Autowired
    private ClusterManager clusterManager;
    
    /**
     * 获取集群状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getClusterStatus() {
        try {
            Map<String, Object> status = clusterManager.getClusterStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取集群状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有节点列表
     */
    @GetMapping("/nodes")
    public ResponseEntity<List<ClusterNode>> getAllNodes() {
        try {
            List<ClusterNode> nodes = clusterManager.getAllNodes();
            return ResponseEntity.ok(nodes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取在线节点列表
     */
    @GetMapping("/nodes/online")
    public ResponseEntity<List<ClusterNode>> getOnlineNodes() {
        try {
            List<ClusterNode> nodes = clusterManager.getOnlineNodes();
            return ResponseEntity.ok(nodes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据节点ID获取节点信息
     */
    @GetMapping("/nodes/{nodeId}")
    public ResponseEntity<ClusterNode> getNodeById(@PathVariable String nodeId) {
        try {
            if (clusterManager.hasNode(nodeId)) {
                ClusterNode node = clusterManager.getNodeById(nodeId);
                return ResponseEntity.ok(node);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取节点数量
     */
    @GetMapping("/nodes/count")
    public ResponseEntity<Map<String, Object>> getNodeCount() {
        try {
            int totalCount = clusterManager.getNodeCount();
            int onlineCount = clusterManager.getOnlineNodes().size();
            int offlineCount = clusterManager.getOfflineNodes().size();
            
            Map<String, Object> count = Map.of(
                "total", totalCount,
                "online", onlineCount,
                "offline", offlineCount
            );
            
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 发送消息到指定节点
     */
    @PostMapping("/nodes/{nodeId}/message")
    public ResponseEntity<Map<String, Object>> sendMessageToNode(
            @PathVariable String nodeId,
            @RequestBody ClusterMessage message) {
        try {
            if (!clusterManager.hasNode(nodeId)) {
                return ResponseEntity.notFound()
                        .body(Map.of("error", "节点不存在: " + nodeId));
            }
            
            CompletableFuture<Void> future = clusterManager.sendMessage(nodeId, message);
            future.get(); // 等待完成
            
            return ResponseEntity.ok(Map.of("message", "消息发送成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "发送消息异常: " + e.getMessage()));
        }
    }
    
    /**
     * 广播消息到所有节点
     */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcastMessage(@RequestBody ClusterMessage message) {
        try {
            CompletableFuture<Void> future = clusterManager.broadcastMessage(message);
            future.get(); // 等待完成
            
            return ResponseEntity.ok(Map.of("message", "广播消息发送成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "广播消息异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取负载均衡信息
     */
    @GetMapping("/loadbalancer")
    public ResponseEntity<Map<String, Object>> getLoadBalancerInfo() {
        try {
            String strategy = clusterManager.getLoadBalancer().getName();
            List<ClusterNode> availableNodes = clusterManager.getOnlineNodes();
            
            Map<String, Object> info = Map.of(
                "strategy", strategy,
                "availableNodes", availableNodes.size(),
                "totalNodes", clusterManager.getNodeCount()
            );
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 选择下一个节点（负载均衡）
     */
    @GetMapping("/loadbalancer/select")
    public ResponseEntity<ClusterNode> selectNextNode() {
        try {
            ClusterNode selectedNode = clusterManager.selectNode();
            if (selectedNode != null) {
                return ResponseEntity.ok(selectedNode);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            boolean isHealthy = clusterManager.getNodeCount() > 0;
            Map<String, Object> health = Map.of(
                "status", isHealthy ? "UP" : "DOWN",
                "timestamp", System.currentTimeMillis(),
                "nodeCount", clusterManager.getNodeCount()
            );
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of(
                        "status", "DOWN",
                        "error", e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }
}
