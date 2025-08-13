package com.example.nettycluster.serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息序列化器工厂
 */
public class MessageSerializerFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageSerializerFactory.class);
    
    private static final Map<String, MessageSerializer> serializers = new HashMap<>();
    
    static {
        // 注册内置的序列化器
        registerSerializer("json", new JsonMessageSerializer());
    }
    
    /**
     * 获取序列化器
     * 
     * @param type 序列化器类型
     * @return 序列化器实例
     */
    public static MessageSerializer getSerializer(String type) {
        MessageSerializer serializer = serializers.get(type);
        if (serializer == null) {
            logger.warn("Serializer type '{}' not found, using json as default", type);
            serializer = serializers.get("json");
        }
        return serializer;
    }
    
    /**
     * 注册序列化器
     * 
     * @param type 序列化器类型
     * @param serializer 序列化器实例
     */
    public static void registerSerializer(String type, MessageSerializer serializer) {
        serializers.put(type, serializer);
        logger.info("Registered serializer: {}", type);
    }
    
    /**
     * 获取所有可用的序列化器类型
     * 
     * @return 序列化器类型列表
     */
    public static String[] getAvailableTypes() {
        return serializers.keySet().toArray(new String[0]);
    }
    
    /**
     * 检查序列化器类型是否存在
     * 
     * @param type 序列化器类型
     * @return 是否存在
     */
    public static boolean hasType(String type) {
        return serializers.containsKey(type);
    }
}
