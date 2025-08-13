package com.example.nettycluster.discovery;

import com.example.nettycluster.config.ClusterConfig;
import com.example.nettycluster.model.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简化的服务发现服务
 * 暂时不依赖Nacos，避免启动问题
 */
@Component
public class SimpleDiscoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleDiscoveryService.class);
    
    @Autowired
    private ClusterConfig clusterConfig;
    
    // 本地缓存的节点信息
    private final Map<String, ClusterNode> discoveredNodes = new ConcurrentHashMap<>();
    
    /**
     * 发现所有Netty集群服务实例
     */
    public List<ClusterNode> discoverClusterNodes() {
        logger.info("使用简化服务发现，返回空列表");
        return List.of();
    }
    
    /**
     * 获取指定服务的所有实例
     */
    public List<ClusterNode> getServiceInstances(String serviceName) {
        logger.info("使用简化服务发现，返回空列表");
        return List.of();
    }
    
    /**
     * 注册本地节点到服务发现
     */
    public void registerLocalNode() {
        try {
            String nodeId = clusterConfig.getNodeId();
            String nodeName = clusterConfig.getNodeName();
            String host = clusterConfig.getNodeHost();
            int port = clusterConfig.getServerPort();
            
            logger.info("本地节点已注册到简化服务发现: {}:{} (ID: {})", host, port, nodeId);
            
        } catch (Exception e) {
            logger.error("注册本地节点失败", e);
        }
    }
    
    /**
     * 注销本地节点
     */
    public void deregisterLocalNode() {
        try {
            String nodeId = clusterConfig.getNodeId();
            String host = clusterConfig.getNodeHost();
            int port = clusterConfig.getServerPort();
            
            logger.info("本地节点已从简化服务发现注销: {}:{} (ID: {})", host, port, nodeId);
            
        } catch (Exception e) {
            logger.error("注销本地节点失败", e);
        }
    }
    
    /**
     * 获取缓存的发现节点
     */
    public Map<String, ClusterNode> getDiscoveredNodes() {
        return new ConcurrentHashMap<>(discoveredNodes);
    }
    
    /**
     * 检查节点是否仍然在线
     */
    public boolean isNodeOnline(String nodeId) {
        ClusterNode node = discoveredNodes.get(nodeId);
        return node != null && node.isOnline();
    }
}
