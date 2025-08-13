package com.example.nettycluster.loadbalancer;

import com.example.nettycluster.model.ClusterNode;
import java.util.List;

/**
 * 负载均衡器接口
 */
public interface LoadBalancer {
    
    /**
     * 选择下一个节点
     * 
     * @param nodes 可用节点列表
     * @return 选中的节点，如果没有可用节点则返回null
     */
    ClusterNode selectNode(List<ClusterNode> nodes);
    
    /**
     * 获取负载均衡器名称
     * 
     * @return 负载均衡器名称
     */
    String getName();
    
    /**
     * 重置负载均衡器状态
     */
    void reset();
}
