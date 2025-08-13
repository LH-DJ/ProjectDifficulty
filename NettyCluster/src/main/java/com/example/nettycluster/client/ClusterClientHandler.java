package com.example.nettycluster.client;

import com.example.nettycluster.model.ClusterMessage;
import com.example.nettycluster.serializer.MessageSerializer;
import com.example.nettycluster.serializer.MessageSerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 集群客户端处理器
 */
public class ClusterClientHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterClientHandler.class);
    
    private final ClusterClient client;
    private final MessageSerializer serializer;
    
    public ClusterClientHandler(ClusterClient client) {
        this.client = client;
        this.serializer = MessageSerializerFactory.getSerializer("json");
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                // 读取数据
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                
                // 反序列化消息
                ClusterMessage message = serializer.deserialize(data);
                logger.debug("Received message from server: {}", message);
                
                // 处理消息
                handleMessage(message);
                
            } finally {
                buf.release();
            }
        } else {
            logger.warn("Received unknown message type: {}", msg.getClass());
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception in client handler", cause);
        ctx.close();
    }
    
    /**
     * 处理接收到的消息
     */
    private void handleMessage(ClusterMessage message) {
        try {
            switch (message.getMessageType()) {
                case RESPONSE:
                    handleResponse(message);
                    break;
                case BROADCAST:
                    handleBroadcast(message);
                    break;
                case HEARTBEAT:
                    handleHeartbeat(message);
                    break;
                default:
                    logger.warn("Unknown message type: {}", message.getMessageType());
            }
        } catch (Exception e) {
            logger.error("Error handling message: {}", message, e);
        }
    }
    
    /**
     * 处理响应消息
     */
    private void handleResponse(ClusterMessage message) {
        client.handleResponse(message);
    }
    
    /**
     * 处理广播消息
     */
    private void handleBroadcast(ClusterMessage message) {
        logger.info("Received broadcast from {}: {}", message.getSourceNodeId(), message.getContent());
        // 这里可以添加广播消息的处理逻辑
    }
    
    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(ClusterMessage message) {
        logger.debug("Received heartbeat from server");
        // 心跳响应通常不需要特殊处理
    }
}
