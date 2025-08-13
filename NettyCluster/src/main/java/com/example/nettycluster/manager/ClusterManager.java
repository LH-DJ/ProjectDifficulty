package com.example.nettycluster.manager;

import com.example.nettycluster.config.ClusterConfig;
import com.example.nettycluster.loadbalancer.LoadBalancer;
import com.example.nettycluster.loadbalancer.LoadBalancerFactory;
import com.example.nettycluster.model.ClusterMessage;
import com.example.nettycluster.model.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 集群管理器
 */
public class ClusterManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);
    
    private final ClusterConfig config;
    private final Map<String, ClusterNode> nodes;
    private final LoadBalancer loadBalancer;
    private final ScheduledExecutorService scheduler;
    private final ReadWriteLock nodesLock;
    
    private volatile boolean running = false;
    private String localNodeId;
    
    public ClusterManager(ClusterConfig config) {
        this.config = config;
        this.nodes = new ConcurrentHashMap<>();
        this.loadBalancer = LoadBalancerFactory.getLoadBalancer(config.getLoadBalancerStrategy());
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.nodesLock = new ReentrantReadWriteLock();
        this.localNodeId = config.getNodeId();
    }
    
    /**
     * 启动集群管理器
     */
    public void start() {
        if (running) {
            logger.warn("Cluster manager is already running");
            return;
        }
        
        logger.info("Starting cluster manager with node ID: {}", localNodeId);
        
        // 启动心跳检测任务
        scheduler.scheduleAtFixedRate(
                this::checkHeartbeats,
                config.getHeartbeatInterval(),
                config.getHeartbeatInterval(),
                TimeUnit.MILLISECONDS
        );
        
        // 启动节点清理任务
        scheduler.scheduleAtFixedRate(
                this::cleanupDeadNodes,
                config.getHeartbeatTimeout() * 2,
                config.getHeartbeatTimeout() * 2,
                TimeUnit.MILLISECONDS
        );
        
        running = true;
        logger.info("Cluster manager started successfully");
    }
    
    /**
     * 停止集群管理器
     */
    public void stop() {
        if (!running) {
            logger.warn("Cluster manager is not running");
            return;
        }
        
        logger.info("Stopping cluster manager");
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        running = false;
        logger.info("Cluster manager stopped");
    }
    
    /**
     * 注册节点
     */
    public void registerNode(ClusterNode node) {
        nodesLock.writeLock().lock();
        try {
            if (nodes.containsKey(node.getNodeId())) {
                logger.warn("Node {} already exists, updating...", node.getNodeId());
            }
            
            nodes.put(node.getNodeId(), node);
            logger.info("Registered node: {}", node);
        } finally {
            nodesLock.writeLock().unlock();
        }
    }
    
    /**
     * 注销节点
     */
    public void unregisterNode(String nodeId) {
        nodesLock.writeLock().lock();
        try {
            ClusterNode removed = nodes.remove(nodeId);
            if (removed != null) {
                logger.info("Unregistered node: {}", removed);
            }
        } finally {
            nodesLock.writeLock().unlock();
        }
    }
    
    /**
     * 更新节点心跳
     */
    public void updateNodeHeartbeat(String nodeId) {
        nodesLock.readLock().lock();
        try {
            ClusterNode node = nodes.get(nodeId);
            if (node != null) {
                node.updateHeartbeat();
                logger.debug("Updated heartbeat for node: {}", nodeId);
            }
        } finally {
            nodesLock.readLock().unlock();
        }
    }
    
    /**
     * 获取所有节点
     */
    public List<ClusterNode> getAllNodes() {
        nodesLock.readLock().lock();
        try {
            return new ArrayList<>(nodes.values());
        } finally {
            nodesLock.readLock().unlock();
        }
    }
    
    /**
     * 获取在线节点
     */
    public List<ClusterNode> getOnlineNodes() {
        nodesLock.readLock().lock();
        try {
            return nodes.values().stream()
                    .filter(ClusterNode::isOnline)
                    .toList();
        } finally {
            nodesLock.readLock().unlock();
        }
    }
    
    /**
     * 获取可用节点
     */
    public List<ClusterNode> getAvailableNodes() {
        nodesLock.readLock().lock();
        try {
            return nodes.values().stream()
                    .filter(ClusterNode::isAvailable)
                    .toList();
        } finally {
            nodesLock.readLock().unlock();
        }
    }
    
    /**
     * 根据负载均衡策略选择节点
     */
    public ClusterNode selectNode() {
        List<ClusterNode> availableNodes = getAvailableNodes();
        return loadBalancer.selectNode(availableNodes);
    }
    
    /**
     * 获取节点数量
     */
    public int getNodeCount() {
        nodesLock.readLock().lock();
        try {
            return nodes.size();
        } finally {
            nodesLock.readLock().unlock();
        }
    }
    
    /**
     * 获取在线节点数量
     */
    public int getOnlineNodeCount() {
        return getOnlineNodes().size();
    }
    
    /**
     * 检查节点是否存在
     */
    public boolean hasNode(String nodeId) {
        nodesLock.readLock().lock();
        try {
            return nodes.containsKey(nodeId);
        } finally {
            nodesLock.readLock().unlock();
        }
    }
    
    /**
     * 获取节点
     */
    public ClusterNode getNode(String nodeId) {
        nodesLock.readLock().lock();
        try {
            return nodes.get(nodeId);
        } finally {
            nodesLock.readLock().unlock();
        }
    }
    
    /**
     * 根据节点ID获取节点信息（别名方法）
     */
    public ClusterNode getNodeById(String nodeId) {
        return getNode(nodeId);
    }
    
    /**
     * 获取离线节点列表
     */
    public List<ClusterNode> getOfflineNodes() {
        nodesLock.readLock().lock();
        try {
            return nodes.values().stream()
                    .filter(node -> !node.isOnline())
                    .collect(Collectors.toList());
        } finally {
            nodesLock.readLock().unlock();
        }
    }
    
    /**
     * 获取负载均衡器
     */
    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }
    
    /**
     * 检查心跳
     */
    private void checkHeartbeats() {
        if (!running) return;
        
        long currentTime = System.currentTimeMillis();
        long timeout = config.getHeartbeatTimeout();
        
        List<ClusterNode> nodesToCheck = getAllNodes();
        for (ClusterNode node : nodesToCheck) {
            if (node.isHeartbeatTimeout(timeout)) {
                logger.warn("Node {} heartbeat timeout, marking as offline", node.getNodeId());
                node.setStatus(ClusterNode.NodeStatus.OFFLINE);
            }
        }
    }
    
    /**
     * 清理死亡节点
     */
    private void cleanupDeadNodes() {
        if (!running) return;
        
        long currentTime = System.currentTimeMillis();
        long cleanupTimeout = config.getHeartbeatTimeout() * 3;
        
        List<String> nodesToRemove = new ArrayList<>();
        List<ClusterNode> nodesToCheck = getAllNodes();
        
        for (ClusterNode node : nodesToCheck) {
            if (node.isHeartbeatTimeout(cleanupTimeout)) {
                nodesToRemove.add(node.getNodeId());
            }
        }
        
        for (String nodeId : nodesToRemove) {
            unregisterNode(nodeId);
            logger.info("Cleaned up dead node: {}", nodeId);
        }
    }
    
    /**
     * 广播消息到所有在线节点
     */
    public CompletableFuture<Void> broadcastMessage(ClusterMessage message) {
        message.setMessageType(ClusterMessage.MessageType.BROADCAST);
        message.setSourceNodeId(localNodeId);
        
        List<ClusterNode> onlineNodes = getOnlineNodes();
        logger.info("Broadcasting message to {} online nodes", onlineNodes.size());
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        // 这里应该实现实际的消息发送逻辑
        // 暂时只是记录日志
        for (ClusterNode node : onlineNodes) {
            if (!node.getNodeId().equals(localNodeId)) {
                logger.debug("Broadcasting to node: {}", node.getNodeId());
            }
        }
        return future;
    }
    
    /**
     * 发送消息到指定节点
     */
    public CompletableFuture<Void> sendMessage(String targetNodeId, ClusterMessage message) {
        message.setTargetNodeId(targetNodeId);
        message.setSourceNodeId(localNodeId);
        
        ClusterNode targetNode = getNode(targetNodeId);
        if (targetNode == null) {
            logger.warn("Target node {} not found", targetNodeId);
            return CompletableFuture.completedFuture(null);
        }
        
        if (!targetNode.isOnline()) {
            logger.warn("Target node {} is offline", targetNodeId);
            return CompletableFuture.completedFuture(null);
        }
        
        logger.debug("Sending message to node: {}", targetNodeId);
        // 这里应该实现实际的消息发送逻辑
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 获取集群状态信息
     */
    public Map<String, Object> getClusterStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("localNodeId", localNodeId);
        status.put("totalNodes", getNodeCount());
        status.put("onlineNodes", getOnlineNodeCount());
        status.put("loadBalancer", loadBalancer.getName());
        status.put("running", running);
        
        List<Map<String, Object>> nodeStatuses = new ArrayList<>();
        for (ClusterNode node : getAllNodes()) {
            Map<String, Object> nodeStatus = new HashMap<>();
            nodeStatus.put("nodeId", node.getNodeId());
            nodeStatus.put("status", node.getStatus());
            nodeStatus.put("lastHeartbeat", node.getLastHeartbeatTime());
            nodeStatus.put("connections", node.getCurrentConnections());
            nodeStatuses.add(nodeStatus);
        }
        status.put("nodes", nodeStatuses);
        
        return status;
    }
    
    // Getters
    public boolean isRunning() {
        return running;
    }
    
    public String getLocalNodeId() {
        return localNodeId;
    }
    
    public ClusterConfig getConfig() {
        return config;
    }
}
