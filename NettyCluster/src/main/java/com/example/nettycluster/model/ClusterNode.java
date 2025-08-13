package com.example.nettycluster.model;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;

/**
 * 集群节点信息
 */
public class ClusterNode implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 节点状态
    public enum NodeStatus {
        ONLINE,     // 在线
        OFFLINE,    // 离线
        SUSPECT,    // 可疑
        FAILED      // 失败
    }
    
    // 节点ID
    private String nodeId;
    
    // 节点名称
    private String nodeName;
    
    // 节点地址
    private String host;
    
    // 节点端口
    private int port;
    
    // 节点状态
    private NodeStatus status;
    
    // 最后心跳时间
    private long lastHeartbeatTime;
    
    // 节点启动时间
    private long startTime;
    
    // 节点版本
    private String version;
    
    // 节点权重 (用于负载均衡)
    private int weight = 1;
    
    // 当前连接数
    private int currentConnections = 0;
    
    // 最大连接数
    private int maxConnections = 1000;
    
    // 节点标签
    private Map<String, String> tags;
    
    // 节点元数据
    private Map<String, Object> metadata;
    
    public ClusterNode() {
        this.tags = new HashMap<>();
        this.metadata = new HashMap<>();
        this.startTime = System.currentTimeMillis();
        this.lastHeartbeatTime = System.currentTimeMillis();
        this.status = NodeStatus.OFFLINE;
    }
    
    public ClusterNode(String nodeId, String host, int port) {
        this();
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.nodeName = nodeId;
    }
    
    // 获取节点地址
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(host, port);
    }
    
    // 获取节点完整地址字符串
    public String getAddressString() {
        return host + ":" + port;
    }
    
    // 检查节点是否在线
    public boolean isOnline() {
        return NodeStatus.ONLINE.equals(this.status);
    }
    
    // 检查节点是否可用
    public boolean isAvailable() {
        return isOnline() && currentConnections < maxConnections;
    }
    
    // 更新心跳时间
    public void updateHeartbeat() {
        this.lastHeartbeatTime = System.currentTimeMillis();
        this.status = NodeStatus.ONLINE;
    }
    
    // 检查心跳是否超时
    public boolean isHeartbeatTimeout(long timeout) {
        return System.currentTimeMillis() - lastHeartbeatTime > timeout;
    }
    
    // 添加标签
    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }
    
    // 获取标签
    public String getTag(String key) {
        return this.tags.get(key);
    }
    
    // 添加元数据
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    // 获取元数据
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    // 增加连接数
    public void incrementConnections() {
        this.currentConnections++;
    }
    
    // 减少连接数
    public void decrementConnections() {
        if (this.currentConnections > 0) {
            this.currentConnections--;
        }
    }
    
    // 获取连接使用率
    public double getConnectionUsage() {
        return maxConnections > 0 ? (double) currentConnections / maxConnections : 0.0;
    }
    
    // Getters and Setters
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public NodeStatus getStatus() {
        return status;
    }
    
    public void setStatus(NodeStatus status) {
        this.status = status;
    }
    
    public long getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }
    
    public void setLastHeartbeatTime(long lastHeartbeatTime) {
        this.lastHeartbeatTime = lastHeartbeatTime;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public void setWeight(int weight) {
        this.weight = weight;
    }
    
    public int getCurrentConnections() {
        return currentConnections;
    }
    
    public void setCurrentConnections(int currentConnections) {
        this.currentConnections = currentConnections;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterNode that = (ClusterNode) o;
        return nodeId != null ? nodeId.equals(that.nodeId) : that.nodeId == null;
    }
    
    @Override
    public int hashCode() {
        return nodeId != null ? nodeId.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "ClusterNode{" +
                "nodeId='" + nodeId + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", status=" + status +
                ", lastHeartbeatTime=" + lastHeartbeatTime +
                ", startTime=" + startTime +
                ", version='" + version + '\'' +
                ", weight=" + weight +
                ", currentConnections=" + currentConnections +
                ", maxConnections=" + maxConnections +
                ", tags=" + tags +
                ", metadata=" + metadata +
                '}';
    }
}
