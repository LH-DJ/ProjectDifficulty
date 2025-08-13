package com.example.nettycluster.loadbalancer;

import com.example.nettycluster.model.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器
 */
public class RoundRobinLoadBalancer implements LoadBalancer {
    
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);
    
    private final AtomicInteger counter = new AtomicInteger(0);
    
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
        
        // 轮询选择
        int index = counter.getAndIncrement() % availableNodes.size();
        ClusterNode selectedNode = availableNodes.get(index);
        
        logger.debug("RoundRobin selected node: {} (index: {})", 
                selectedNode.getNodeId(), index);
        
        return selectedNode;
    }
    
    @Override
    public String getName() {
        return "round-robin";
    }
    
    @Override
    public void reset() {
        counter.set(0);
        logger.debug("RoundRobin load balancer reset");
    }
}
