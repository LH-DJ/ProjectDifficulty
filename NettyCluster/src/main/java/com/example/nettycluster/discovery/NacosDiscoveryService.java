package com.example.nettycluster.discovery;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.nettycluster.config.ClusterConfig;
import com.example.nettycluster.model.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Nacos服务发现集成类
 * 集成Spring Cloud Alibaba Nacos进行服务发现
 */
@Component
public class NacosDiscoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(NacosDiscoveryService.class);
    
    @Autowired
    private NacosServiceDiscovery nacosServiceDiscovery;
    
    @Autowired
    private NacosServiceManager nacosServiceManager;
    
    @Autowired
    private ClusterConfig clusterConfig;
    
    // 本地缓存的节点信息
    private final Map<String, ClusterNode> discoveredNodes = new ConcurrentHashMap<>();
    
    /**
     * 发现所有Netty集群服务实例
     */
    public List<ClusterNode> discoverClusterNodes() {
        try {
            String serviceName = clusterConfig.getNode().getName();
            List<ServiceInstance> instances = nacosServiceDiscovery.getInstances(serviceName);
            
            List<ClusterNode> nodes = instances.stream()
                    .map(this::convertToClusterNode)
                    .collect(Collectors.toList());
            
            // 更新本地缓存
            updateDiscoveredNodes(nodes);
            
            logger.info("发现到 {} 个集群节点", nodes.size());
            return nodes;
            
        } catch (Exception e) {
            logger.error("服务发现失败", e);
            return List.of();
        }
    }
    
    /**
     * 获取指定服务的所有实例
     */
    public List<ClusterNode> getServiceInstances(String serviceName) {
        try {
            List<ServiceInstance> instances = nacosServiceDiscovery.getInstances(serviceName);
            return instances.stream()
                    .map(this::convertToClusterNode)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取服务实例失败: {}", serviceName, e);
            return List.of();
        }
    }
    
    /**
     * 注册本地节点到Nacos
     */
    public void registerLocalNode() {
        try {
            String serviceName = clusterConfig.getNode().getName();
            String nodeId = clusterConfig.getNode().getId();
            String host = clusterConfig.getNode().getHost();
            int port = clusterConfig.getServerPort();
            
            // 创建Nacos实例
            Instance instance = new Instance();
            instance.setInstanceId(nodeId);
            instance.setServiceName(serviceName);
            instance.setIp(host);
            instance.setPort(port);
            instance.setHealthy(true);
            instance.setEnabled(true);
            
            // 设置元数据
            Map<String, String> metadata = Map.of(
                "nodeId", nodeId,
                "nodeName", clusterConfig.getNode().getName(),
                "nettyPort", String.valueOf(port),
                "version", "1.0.0"
            );
            instance.setMetadata(metadata);
            
            // 注册到Nacos
            nacosServiceManager.getNamingService().registerInstance(serviceName, instance);
            
            logger.info("本地节点已注册到Nacos: {}:{}", host, port);
            
        } catch (Exception e) {
            logger.error("注册本地节点到Nacos失败", e);
        }
    }
    
    /**
     * 注销本地节点
     */
    public void deregisterLocalNode() {
        try {
            String serviceName = clusterConfig.getNode().getName();
            String nodeId = clusterConfig.getNode().getId();
            String host = clusterConfig.getNode().getHost();
            int port = clusterConfig.getServerPort();
            
            nacosServiceManager.getNamingService().deregisterInstance(serviceName, host, port);
            
            logger.info("本地节点已从Nacos注销: {}:{}", host, port);
            
        } catch (Exception e) {
            logger.error("从Nacos注销本地节点失败", e);
        }
    }
    
    /**
     * 将ServiceInstance转换为ClusterNode
     */
    private ClusterNode convertToClusterNode(ServiceInstance instance) {
        ClusterNode node = new ClusterNode();
        node.setNodeId(instance.getInstanceId());
        node.setNodeName(instance.getServiceId());
        node.setHost(instance.getHost());
        node.setPort(instance.getPort());
        node.setStatus(ClusterNode.NodeStatus.ONLINE);
        node.setLastHeartbeatTime(System.currentTimeMillis());
        node.setStartTime(System.currentTimeMillis());
        
        // 从元数据中获取额外信息
        Map<String, String> metadata = instance.getMetadata();
        if (metadata != null) {
            String nettyPort = metadata.get("nettyPort");
            if (nettyPort != null) {
                try {
                    node.setPort(Integer.parseInt(nettyPort));
                } catch (NumberFormatException e) {
                    logger.warn("无效的Netty端口: {}", nettyPort);
                }
            }
            
            String version = metadata.get("version");
            if (version != null) {
                node.setVersion(version);
            }
        }
        
        return node;
    }
    
    /**
     * 更新发现的节点缓存
     */
    private void updateDiscoveredNodes(List<ClusterNode> nodes) {
        discoveredNodes.clear();
        for (ClusterNode node : nodes) {
            discoveredNodes.put(node.getNodeId(), node);
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
