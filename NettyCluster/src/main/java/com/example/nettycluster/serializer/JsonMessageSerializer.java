package com.example.nettycluster.serializer;

import com.example.nettycluster.model.ClusterMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON消息序列化器
 */
public class JsonMessageSerializer implements MessageSerializer {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonMessageSerializer.class);
    
    private final ObjectMapper objectMapper;
    
    public JsonMessageSerializer() {
        this.objectMapper = new ObjectMapper();
        // 配置ObjectMapper
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    @Override
    public byte[] serialize(ClusterMessage message) throws Exception {
        try {
            String json = objectMapper.writeValueAsString(message);
            logger.debug("Serialized message to JSON: {}", json);
            return json.getBytes("UTF-8");
        } catch (Exception e) {
            logger.error("Failed to serialize message: {}", message, e);
            throw e;
        }
    }
    
    @Override
    public ClusterMessage deserialize(byte[] data) throws Exception {
        try {
            String json = new String(data, "UTF-8");
            logger.debug("Deserializing JSON: {}", json);
            ClusterMessage message = objectMapper.readValue(json, ClusterMessage.class);
            logger.debug("Deserialized message: {}", message);
            return message;
        } catch (Exception e) {
            logger.error("Failed to deserialize data: {}", new String(data), e);
            throw e;
        }
    }
    
    @Override
    public String getName() {
        return "json";
    }
    
    @Override
    public String getMimeType() {
        return "application/json";
    }
}
