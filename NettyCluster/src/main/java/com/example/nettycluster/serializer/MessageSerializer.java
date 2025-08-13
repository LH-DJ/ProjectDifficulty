package com.example.nettycluster.serializer;

import com.example.nettycluster.model.ClusterMessage;

/**
 * 消息序列化器接口
 */
public interface MessageSerializer {
    
    /**
     * 序列化消息
     * 
     * @param message 集群消息
     * @return 序列化后的字节数组
     * @throws Exception 序列化异常
     */
    byte[] serialize(ClusterMessage message) throws Exception;
    
    /**
     * 反序列化消息
     * 
     * @param data 字节数组
     * @return 集群消息
     * @throws Exception 反序列化异常
     */
    ClusterMessage deserialize(byte[] data) throws Exception;
    
    /**
     * 获取序列化器名称
     * 
     * @return 序列化器名称
     */
    String getName();
    
    /**
     * 获取序列化器MIME类型
     * 
     * @return MIME类型
     */
    String getMimeType();
}
