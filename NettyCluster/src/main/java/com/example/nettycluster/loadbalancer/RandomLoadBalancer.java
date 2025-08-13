package com.example.nettycluster.loadbalancer;

import com.example.nettycluster.model.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡器
 */
public class RandomLoadBalancer implements LoadBalancer {
    
    private static final Logger logger = LoggerFactory.getLogger(RandomLoadBalancer.class);
    
    private final Random random = new Random();
    
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
        
        // 随机选择
        int index = random.nextInt(availableNodes.size());
        ClusterNode selectedNode = availableNodes.get(index);
        
        logger.debug("Random selected node: {} (index: {})", 
                selectedNode.getNodeId(), index);
        
        return selectedNode;
    }
    
    @Override
    public String getName() {
        return "random";
    }
    
    @Override
    public void reset() {
        // 随机负载均衡器不需要重置状态
        logger.debug("Random load balancer reset");
    }
}
