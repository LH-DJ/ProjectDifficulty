package com.example.nettycluster.server;

import com.example.nettycluster.manager.ClusterManager;
import com.example.nettycluster.model.ClusterMessage;
import com.example.nettycluster.model.ClusterNode;
import com.example.nettycluster.serializer.MessageSerializer;
import com.example.nettycluster.serializer.MessageSerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 集群服务器处理器
 */
public class ClusterServerHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterServerHandler.class);
    
    private final ClusterManager clusterManager;
    private final MessageSerializer serializer;
    private final AtomicLong connectionCounter;
    
    private String clientNodeId;
    private SocketChannel socketChannel;
    
    public ClusterServerHandler(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        this.serializer = MessageSerializerFactory.getSerializer("json");
        this.connectionCounter = new AtomicLong(0);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.socketChannel = (SocketChannel) ctx.channel();
        InetSocketAddress address = socketChannel.remoteAddress();
        
        // 生成客户端节点ID
        this.clientNodeId = "client-" + address.getHostString() + "-" + address.getPort() + "-" + connectionCounter.incrementAndGet();
        
        // 创建客户端节点
        ClusterNode clientNode = new ClusterNode(clientNodeId, address.getHostString(), address.getPort());
        clientNode.setStatus(ClusterNode.NodeStatus.ONLINE);
        clientNode.updateHeartbeat();
        
        // 注册到集群管理器
        clusterManager.registerNode(clientNode);
        
        logger.info("Client connected: {} from {}", clientNodeId, address);
        
        // 发送欢迎消息
        ClusterMessage welcomeMsg = new ClusterMessage(ClusterMessage.MessageType.RESPONSE, "Welcome to Netty Cluster!");
        welcomeMsg.setTargetNodeId(clientNodeId);
        welcomeMsg.setSourceNodeId(clusterManager.getLocalNodeId());
        sendMessage(welcomeMsg);
        
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (clientNodeId != null) {
            // 注销客户端节点
            clusterManager.unregisterNode(clientNodeId);
            logger.info("Client disconnected: {}", clientNodeId);
        }
        super.channelInactive(ctx);
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
                message.setSourceNodeId(clientNodeId);
                
                logger.debug("Received message from {}: {}", clientNodeId, message);
                
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
        logger.error("Exception in channel handler for client: {}", clientNodeId, cause);
        ctx.close();
    }
    
    /**
     * 处理接收到的消息
     */
    private void handleMessage(ClusterMessage message) {
        try {
            switch (message.getMessageType()) {
                case HEARTBEAT:
                    handleHeartbeat(message);
                    break;
                case REGISTER:
                    handleRegister(message);
                    break;
                case DATA:
                    handleData(message);
                    break;
                case COMMAND:
                    handleCommand(message);
                    break;
                case BROADCAST:
                    handleBroadcast(message);
                    break;
                default:
                    logger.warn("Unknown message type: {}", message.getMessageType());
                    sendErrorResponse(message, "Unknown message type");
            }
        } catch (Exception e) {
            logger.error("Error handling message: {}", message, e);
            sendErrorResponse(message, "Internal error: " + e.getMessage());
        }
    }
    
    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(ClusterMessage message) {
        // 更新客户端心跳
        clusterManager.updateNodeHeartbeat(clientNodeId);
        
        // 发送心跳响应
        ClusterMessage response = new ClusterMessage(ClusterMessage.MessageType.RESPONSE, "Heartbeat received");
        response.setTargetNodeId(clientNodeId);
        response.setSourceNodeId(clusterManager.getLocalNodeId());
        response.addAttribute("timestamp", System.currentTimeMillis());
        
        sendMessage(response);
        logger.debug("Heartbeat from {} processed", clientNodeId);
    }
    
    /**
     * 处理注册消息
     */
    private void handleRegister(ClusterMessage message) {
        // 更新客户端节点信息
        ClusterNode clientNode = clusterManager.getNode(clientNodeId);
        if (clientNode != null) {
            // 更新节点信息
            if (message.getContent() != null) {
                clientNode.setNodeName(message.getContent());
            }
            
            // 更新元数据
            if (message.getAttributes() != null) {
                message.getAttributes().forEach(clientNode::addMetadata);
            }
            
            clusterManager.registerNode(clientNode);
            
            // 发送注册成功响应
            ClusterMessage response = new ClusterMessage(ClusterMessage.MessageType.RESPONSE, "Registration successful");
            response.setTargetNodeId(clientNodeId);
            response.setSourceNodeId(clusterManager.getLocalNodeId());
            response.addAttribute("nodeId", clientNodeId);
            
            sendMessage(response);
            logger.info("Client {} registered successfully", clientNodeId);
        }
    }
    
    /**
     * 处理数据消息
     */
    private void handleData(ClusterMessage message) {
        logger.info("Received data message from {}: {}", clientNodeId, message.getContent());
        
        // 这里可以添加数据处理逻辑
        // 例如：转发到其他节点、存储到数据库等
        
        // 发送确认响应
        ClusterMessage response = new ClusterMessage(ClusterMessage.MessageType.RESPONSE, "Data received");
        response.setTargetNodeId(clientNodeId);
        response.setSourceNodeId(clusterManager.getLocalNodeId());
        response.addAttribute("receivedAt", System.currentTimeMillis());
        
        sendMessage(response);
    }
    
    /**
     * 处理命令消息
     */
    private void handleCommand(ClusterMessage message) {
        String command = message.getContent();
        logger.info("Received command from {}: {}", clientNodeId, command);
        
        String responseContent = "Command executed: " + command;
        
        // 这里可以添加命令处理逻辑
        switch (command.toLowerCase()) {
            case "status":
                responseContent = "Cluster status: " + clusterManager.getClusterStatus();
                break;
            case "nodes":
                responseContent = "Online nodes: " + clusterManager.getOnlineNodeCount();
                break;
            default:
                responseContent = "Unknown command: " + command;
        }
        
        ClusterMessage response = new ClusterMessage(ClusterMessage.MessageType.RESPONSE, responseContent);
        response.setTargetNodeId(clientNodeId);
        response.setSourceNodeId(clusterManager.getLocalNodeId());
        
        sendMessage(response);
    }
    
    /**
     * 处理广播消息
     */
    private void handleBroadcast(ClusterMessage message) {
        logger.info("Received broadcast from {}: {}", clientNodeId, message.getContent());
        
        // 转发广播消息到其他节点
        clusterManager.broadcastMessage(message);
        
        // 发送确认响应
        ClusterMessage response = new ClusterMessage(ClusterMessage.MessageType.RESPONSE, "Broadcast sent");
        response.setTargetNodeId(clientNodeId);
        response.setSourceNodeId(clusterManager.getLocalNodeId());
        
        sendMessage(response);
    }
    
    /**
     * 发送消息到客户端
     */
    private void sendMessage(ClusterMessage message) {
        try {
            byte[] data = serializer.serialize(message);
            ByteBuf buf = socketChannel.alloc().buffer(data.length);
            buf.writeBytes(data);
            
            socketChannel.writeAndFlush(buf).addListener(future -> {
                if (!future.isSuccess()) {
                    logger.error("Failed to send message to client: {}", clientNodeId, future.cause());
                }
            });
            
            logger.debug("Sent message to {}: {}", clientNodeId, message);
        } catch (Exception e) {
            logger.error("Error sending message to client: {}", clientNodeId, e);
        }
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ClusterMessage originalMessage, String errorMessage) {
        ClusterMessage errorResponse = new ClusterMessage(ClusterMessage.MessageType.RESPONSE, "Error: " + errorMessage);
        errorResponse.setTargetNodeId(clientNodeId);
        errorResponse.setSourceNodeId(clusterManager.getLocalNodeId());
        errorResponse.addAttribute("error", true);
        errorResponse.addAttribute("originalMessageId", originalMessage.getMessageId());
        
        sendMessage(errorResponse);
    }
    
    // Getters
    public String getClientNodeId() {
        return clientNodeId;
    }
    
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
}
