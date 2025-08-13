package com.example.nettycluster.model;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * 集群消息模型
 */
public class ClusterMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 消息类型
    public enum MessageType {
        HEARTBEAT,      // 心跳消息
        REGISTER,       // 节点注册
        UNREGISTER,     // 节点注销
        DATA,           // 数据消息
        COMMAND,        // 命令消息
        RESPONSE,       // 响应消息
        BROADCAST       // 广播消息
    }
    
    // 消息ID
    private String messageId;
    
    // 消息类型
    private MessageType messageType;
    
    // 源节点ID
    private String sourceNodeId;
    
    // 目标节点ID (null表示广播)
    private String targetNodeId;
    
    // 时间戳
    private long timestamp;
    
    // 消息内容
    private String content;
    
    // 消息数据
    private byte[] data;
    
    // 扩展属性
    private Map<String, Object> attributes;
    
    // 序列号
    private long sequenceNumber;
    
    public ClusterMessage() {
        this.messageId = generateMessageId();
        this.timestamp = System.currentTimeMillis();
        this.attributes = new HashMap<>();
        this.sequenceNumber = 0;
    }
    
    public ClusterMessage(MessageType messageType) {
        this();
        this.messageType = messageType;
    }
    
    public ClusterMessage(MessageType messageType, String content) {
        this(messageType);
        this.content = content;
    }
    
    // 生成消息ID
    private String generateMessageId() {
        return "msg-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
    
    // 添加属性
    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
    
    // 获取属性
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }
    
    // 获取属性，带默认值
    public Object getAttribute(String key, Object defaultValue) {
        return this.attributes.getOrDefault(key, defaultValue);
    }
    
    // 检查是否为心跳消息
    public boolean isHeartbeat() {
        return MessageType.HEARTBEAT.equals(this.messageType);
    }
    
    // 检查是否为广播消息
    public boolean isBroadcast() {
        return MessageType.BROADCAST.equals(this.messageType);
    }
    
    // 检查是否为响应消息
    public boolean isResponse() {
        return MessageType.RESPONSE.equals(this.messageType);
    }
    
    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public String getSourceNodeId() {
        return sourceNodeId;
    }
    
    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }
    
    public String getTargetNodeId() {
        return targetNodeId;
    }
    
    public void setTargetNodeId(String targetNodeId) {
        this.targetNodeId = targetNodeId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public long getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    @Override
    public String toString() {
        return "ClusterMessage{" +
                "messageId='" + messageId + '\'' +
                ", messageType=" + messageType +
                ", sourceNodeId='" + sourceNodeId + '\'' +
                ", targetNodeId='" + targetNodeId + '\'' +
                ", timestamp=" + timestamp +
                ", content='" + content + '\'' +
                ", dataLength=" + (data != null ? data.length : 0) +
                ", attributes=" + attributes +
                ", sequenceNumber=" + sequenceNumber +
                '}';
    }
}
