package com.example.nettycluster.loadbalancer;

import com.example.nettycluster.model.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Comparator;

/**
 * 最少连接数负载均衡器
 */
public class LeastConnectionsLoadBalancer implements LoadBalancer {
    
    private static final Logger logger = LoggerFactory.getLogger(LeastConnectionsLoadBalancer.class);
    
    @Override
    public ClusterNode selectNode(List<ClusterNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            logger.warn("No available nodes for load balancing");
            return null;
        }
        
        // 过滤出可用的节点
        List<ClusterNode> availableNodes = nodes.stream()
                .filter(ClusterNode::isAvailable)
                .toList();
        
        if (availableNodes.isEmpty()) {
            logger.warn("No available nodes found after filtering");
            return null;
        }
        
        // 选择连接数最少的节点
        ClusterNode selectedNode = availableNodes.stream()
                .min(Comparator.comparingInt(ClusterNode::getCurrentConnections))
                .orElse(null);
        
        if (selectedNode != null) {
            logger.debug("LeastConnections selected node: {} (connections: {})", 
                    selectedNode.getNodeId(), selectedNode.getCurrentConnections());
        }
        
        return selectedNode;
    }
    
    @Override
    public String getName() {
        return "least-connections";
    }
    
    @Override
    public void reset() {
        // 最少连接数负载均衡器不需要重置状态
        logger.debug("LeastConnections load balancer reset");
    }
}
