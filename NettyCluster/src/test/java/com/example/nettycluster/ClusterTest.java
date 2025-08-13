package com.example.nettycluster;

import com.example.nettycluster.config.ClusterConfig;
import com.example.nettycluster.loadbalancer.LoadBalancer;
import com.example.nettycluster.loadbalancer.LoadBalancerFactory;
import com.example.nettycluster.model.ClusterMessage;
import com.example.nettycluster.model.ClusterNode;
import com.example.nettycluster.serializer.MessageSerializer;
import com.example.nettycluster.serializer.MessageSerializerFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 集群功能测试类
 */
public class ClusterTest {
    
    @Test
    public void testClusterConfig() {
        ClusterConfig config = new ClusterConfig();
        
        config.setServerPort(9090);
        config.setClusterName("test-cluster");
        config.setHeartbeatInterval(3000);
        
        assertEquals(9090, config.getServerPort());
        assertEquals("test-cluster", config.getClusterName());
        assertEquals(3000, config.getHeartbeatInterval());
        assertNotNull(config.getNodeId());
    }
    
    @Test
    public void testClusterMessage() {
        ClusterMessage message = new ClusterMessage(ClusterMessage.MessageType.DATA, "Hello World");
        message.setSourceNodeId("node-1");
        message.setTargetNodeId("node-2");
        
        assertEquals(ClusterMessage.MessageType.DATA, message.getMessageType());
        assertEquals("Hello World", message.getContent());
        assertEquals("node-1", message.getSourceNodeId());
        assertEquals("node-2", message.getTargetNodeId());
        assertNotNull(message.getMessageId());
        assertTrue(message.getTimestamp() > 0);
    }
    
    @Test
    public void testClusterNode() {
        ClusterNode node = new ClusterNode("test-node", "localhost", 8080);
        node.setStatus(ClusterNode.NodeStatus.ONLINE);
        node.setWeight(2);
        
        assertEquals("test-node", node.getNodeId());
        assertEquals("localhost", node.getHost());
        assertEquals(8080, node.getPort());
        assertEquals(ClusterNode.NodeStatus.ONLINE, node.getStatus());
        assertEquals(2, node.getWeight());
        assertTrue(node.isOnline());
        assertTrue(node.isAvailable());
    }
    
    @Test
    public void testLoadBalancerFactory() {
        // 测试获取负载均衡器
        LoadBalancer roundRobin = LoadBalancerFactory.getLoadBalancer("round-robin");
        assertNotNull(roundRobin);
        assertEquals("round-robin", roundRobin.getName());
        
        LoadBalancer random = LoadBalancerFactory.getLoadBalancer("random");
        assertNotNull(random);
        assertEquals("random", random.getName());
        
        LoadBalancer leastConnections = LoadBalancerFactory.getLoadBalancer("least-connections");
        assertNotNull(leastConnections);
        assertEquals("least-connections", leastConnections.getName());
        
        // 测试默认策略
        LoadBalancer defaultLB = LoadBalancerFactory.getLoadBalancer("unknown");
        assertNotNull(defaultLB);
        assertEquals("round-robin", defaultLB.getName());
    }
    
    @Test
    public void testMessageSerializerFactory() {
        // 测试获取序列化器
        MessageSerializer jsonSerializer = MessageSerializerFactory.getSerializer("json");
        assertNotNull(jsonSerializer);
        assertEquals("json", jsonSerializer.getName());
        assertEquals("application/json", jsonSerializer.getMimeType());
        
        // 测试默认序列化器
        MessageSerializer defaultSerializer = MessageSerializerFactory.getSerializer("unknown");
        assertNotNull(defaultSerializer);
        assertEquals("json", defaultSerializer.getName());
    }
    
    @Test
    public void testRoundRobinLoadBalancer() {
        LoadBalancer loadBalancer = LoadBalancerFactory.getLoadBalancer("round-robin");
        
        // 创建测试节点
        ClusterNode node1 = new ClusterNode("node1", "localhost", 8081);
        ClusterNode node2 = new ClusterNode("node2", "localhost", 8082);
        ClusterNode node3 = new ClusterNode("node3", "localhost", 8083);
        
        node1.setStatus(ClusterNode.NodeStatus.ONLINE);
        node2.setStatus(ClusterNode.NodeStatus.ONLINE);
        node3.setStatus(ClusterNode.NodeStatus.ONLINE);
        
        java.util.List<ClusterNode> nodes = java.util.Arrays.asList(node1, node2, node3);
        
        // 测试轮询选择
        ClusterNode selected1 = loadBalancer.selectNode(nodes);
        ClusterNode selected2 = loadBalancer.selectNode(nodes);
        ClusterNode selected3 = loadBalancer.selectNode(nodes);
        ClusterNode selected4 = loadBalancer.selectNode(nodes);
        
        assertNotNull(selected1);
        assertNotNull(selected2);
        assertNotNull(selected3);
        assertNotNull(selected4);
        
        // 验证轮询顺序
        assertEquals(selected1, selected4);
    }
    
    @Test
    public void testRandomLoadBalancer() {
        LoadBalancer loadBalancer = LoadBalancerFactory.getLoadBalancer("random");
        
        ClusterNode node1 = new ClusterNode("node1", "localhost", 8081);
        ClusterNode node2 = new ClusterNode("node2", "localhost", 8082);
        
        node1.setStatus(ClusterNode.NodeStatus.ONLINE);
        node2.setStatus(ClusterNode.NodeStatus.ONLINE);
        
        java.util.List<ClusterNode> nodes = java.util.Arrays.asList(node1, node2);
        
        // 测试随机选择
        ClusterNode selected = loadBalancer.selectNode(nodes);
        assertNotNull(selected);
        assertTrue(selected == node1 || selected == node2);
    }
    
    @Test
    public void testLeastConnectionsLoadBalancer() {
        LoadBalancer loadBalancer = LoadBalancerFactory.getLoadBalancer("least-connections");
        
        ClusterNode node1 = new ClusterNode("node1", "localhost", 8081);
        ClusterNode node2 = new ClusterNode("node2", "localhost", 8082);
        
        node1.setStatus(ClusterNode.NodeStatus.ONLINE);
        node2.setStatus(ClusterNode.NodeStatus.ONLINE);
        
        // 设置不同的连接数
        node1.setCurrentConnections(5);
        node2.setCurrentConnections(2);
        
        java.util.List<ClusterNode> nodes = java.util.Arrays.asList(node1, node2);
        
        // 测试最少连接数选择
        ClusterNode selected = loadBalancer.selectNode(nodes);
        assertNotNull(selected);
        assertEquals(node2, selected); // 应该选择连接数最少的节点
    }
    
    @Test
    public void testJsonMessageSerializer() throws Exception {
        MessageSerializer serializer = MessageSerializerFactory.getSerializer("json");
        
        // 创建测试消息
        ClusterMessage originalMessage = new ClusterMessage(ClusterMessage.MessageType.DATA, "Test Message");
        originalMessage.setSourceNodeId("source");
        originalMessage.setTargetNodeId("target");
        originalMessage.addAttribute("key", "value");
        
        // 序列化
        byte[] serialized = serializer.serialize(originalMessage);
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
        
        // 反序列化
        ClusterMessage deserializedMessage = serializer.deserialize(serialized);
        assertNotNull(deserializedMessage);
        assertEquals(originalMessage.getMessageType(), deserializedMessage.getMessageType());
        assertEquals(originalMessage.getContent(), deserializedMessage.getContent());
        assertEquals(originalMessage.getSourceNodeId(), deserializedMessage.getSourceNodeId());
        assertEquals(originalMessage.getTargetNodeId(), deserializedMessage.getTargetNodeId());
        assertEquals(originalMessage.getAttribute("key"), deserializedMessage.getAttribute("key"));
    }
    
    @Test
    public void testLoadBalancerWithOfflineNodes() {
        LoadBalancer loadBalancer = LoadBalancerFactory.getLoadBalancer("round-robin");
        
        ClusterNode onlineNode = new ClusterNode("online", "localhost", 8081);
        ClusterNode offlineNode = new ClusterNode("offline", "localhost", 8082);
        
        onlineNode.setStatus(ClusterNode.NodeStatus.ONLINE);
        offlineNode.setStatus(ClusterNode.NodeStatus.OFFLINE);
        
        java.util.List<ClusterNode> nodes = java.util.Arrays.asList(onlineNode, offlineNode);
        
        // 应该只选择在线节点
        ClusterNode selected = loadBalancer.selectNode(nodes);
        assertNotNull(selected);
        assertEquals(onlineNode, selected);
    }
    
    @Test
    public void testLoadBalancerWithNoAvailableNodes() {
        LoadBalancer loadBalancer = LoadBalancerFactory.getLoadBalancer("round-robin");
        
        ClusterNode offlineNode = new ClusterNode("offline", "localhost", 8081);
        offlineNode.setStatus(ClusterNode.NodeStatus.OFFLINE);
        
        java.util.List<ClusterNode> nodes = java.util.Arrays.asList(offlineNode);
        
        // 没有可用节点时应该返回null
        ClusterNode selected = loadBalancer.selectNode(nodes);
        assertNull(selected);
    }
}
