package com.example.nettycluster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * Netty集群配置类
 * 支持Spring Boot配置属性绑定
 */
@Component
@ConfigurationProperties(prefix = "netty.cluster")
public class ClusterConfig {
    
    // 服务器配置
    private Server server = new Server();
    
    // 节点配置
    private Node node = new Node();
    
    // 服务发现配置
    private Discovery discovery = new Discovery();
    
    // 负载均衡配置
    private LoadBalancer loadBalancer = new LoadBalancer();
    
    // 序列化配置
    private Serialization serialization = new Serialization();
    
    // 构造函数
    public ClusterConfig() {}
    
    // Getter和Setter方法
    public Server getServer() { return server; }
    public void setServer(Server server) { this.server = server; }
    
    public Node getNode() { return node; }
    public void setNode(Node node) { this.node = node; }
    
    public Discovery getDiscovery() { return discovery; }
    public void setDiscovery(Discovery discovery) { this.discovery = discovery; }
    
    public LoadBalancer getLoadBalancer() { return loadBalancer; }
    public void setLoadBalancer(LoadBalancer loadBalancer) { this.loadBalancer = loadBalancer; }
    
    public Serialization getSerialization() { return serialization; }
    public void setSerialization(Serialization serialization) { this.serialization = serialization; }
    
    // 便捷方法
    public int getServerPort() { return server.getPort(); }
    public int getBossThreads() { return server.getBossThreads(); }
    public int getWorkerThreads() { return server.getWorkerThreads(); }
    public int getMaxConnections() { return server.getMaxConnections(); }
    public String getNodeId() { return node.getId(); }
    public String getNodeName() { return node.getName(); }
    public String getNodeHost() { return node.getHost(); }
    public int getNodePort() { return node.getPort(); }
    public long getHeartbeatInterval() { return discovery.getHeartbeatInterval(); }
    public long getHeartbeatTimeout() { return discovery.getHeartbeatTimeout(); }
    public List<String> getSeedNodes() { return discovery.getSeedNodes(); }
    public String getLoadBalancerStrategy() { return loadBalancer.getStrategy(); }
    public String getSerializationType() { return serialization.getType(); }
    
    /**
     * 服务器配置内部类
     */
    public static class Server {
        private int port = 9090;
        private int bossThreads = 1;
        private int workerThreads = 4;
        private int maxConnections = 1000;
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public int getBossThreads() { return bossThreads; }
        public void setBossThreads(int bossThreads) { this.bossThreads = bossThreads; }
        
        public int getWorkerThreads() { return workerThreads; }
        public void setWorkerThreads(int workerThreads) { this.workerThreads = workerThreads; }
        
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    }
    
    /**
     * 节点配置内部类
     */
    public static class Node {
        private String id;
        private String name;
        private String host = "localhost";
        private int port = 9090;
        private Map<String, String> metadata;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }
    
    /**
     * 服务发现配置内部类
     */
    public static class Discovery {
        private long heartbeatInterval = 30000;
        private long heartbeatTimeout = 90000;
        private List<String> seedNodes;
        
        public long getHeartbeatInterval() { return heartbeatInterval; }
        public void setHeartbeatInterval(long heartbeatInterval) { this.heartbeatInterval = heartbeatInterval; }
        
        public long getHeartbeatTimeout() { return heartbeatTimeout; }
        public void setHeartbeatTimeout(long heartbeatTimeout) { this.heartbeatTimeout = heartbeatTimeout; }
        
        public List<String> getSeedNodes() { return seedNodes; }
        public void setSeedNodes(List<String> seedNodes) { this.seedNodes = seedNodes; }
    }
    
    /**
     * 负载均衡配置内部类
     */
    public static class LoadBalancer {
        private String strategy = "round-robin";
        
        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }
    }
    
    /**
     * 序列化配置内部类
     */
    public static class Serialization {
        private String type = "json";
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
    
    @Override
    public String toString() {
        return "ClusterConfig{" +
                "server=" + server +
                ", node=" + node +
                ", discovery=" + discovery +
                ", loadBalancer=" + loadBalancer +
                ", serialization=" + serialization +
                '}';
    }
}
