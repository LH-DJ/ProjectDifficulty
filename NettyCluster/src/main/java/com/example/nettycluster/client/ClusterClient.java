package com.example.nettycluster.client;

import com.example.nettycluster.config.ClusterConfig;
import com.example.nettycluster.model.ClusterMessage;
import com.example.nettycluster.serializer.MessageSerializer;
import com.example.nettycluster.serializer.MessageSerializerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

/**
 * Netty集群客户端
 */
public class ClusterClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterClient.class);
    
    private final ClusterConfig config;
    private final String clientId;
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private final MessageSerializer serializer;
    private final ScheduledExecutorService scheduler;
    private final Map<String, CompletableFuture<ClusterMessage>> pendingRequests;
    private final AtomicLong requestIdCounter;
    
    private Channel channel;
    private volatile boolean connected = false;
    private volatile boolean running = false;
    
    public ClusterClient(ClusterConfig config) {
        this.config = config;
        this.clientId = "client-" + System.currentTimeMillis();
        this.group = new NioEventLoopGroup(1);
        this.bootstrap = new Bootstrap();
        this.serializer = MessageSerializerFactory.getSerializer(config.getSerializationType());
        this.scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);
        this.pendingRequests = new ConcurrentHashMap<>();
        this.requestIdCounter = new AtomicLong(0);
        
        initializeBootstrap();
    }
    
    /**
     * 初始化Bootstrap
     */
    private void initializeBootstrap() {
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 4));
                        ch.pipeline().addLast(new LengthFieldPrepender(4));
                        ch.pipeline().addLast(new ClusterClientHandler(ClusterClient.this));
                    }
                });
    }
    
    /**
     * 连接到服务器
     */
    public CompletableFuture<Boolean> connect(String host, int port) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (connected) {
            future.complete(true);
            return future;
        }
        
        logger.info("Connecting to server {}:{}", host, port);
        
        bootstrap.connect(host, port).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                this.channel = channelFuture.channel();
                this.connected = true;
                this.running = true;
                
                logger.info("Connected to server {}:{}", host, port);
                
                // 启动心跳任务
                startHeartbeat();
                
                // 发送注册消息
                registerToServer();
                
                future.complete(true);
            } else {
                logger.error("Failed to connect to server {}:{}", host, port, channelFuture.cause());
                future.completeExceptionally(channelFuture.cause());
            }
        });
        
        return future;
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
        logger.info("Disconnecting from server");
        
        running = false;
        connected = false;
        
        if (channel != null) {
            channel.close();
            channel = null;
        }
        
        // 取消所有待处理的请求
        pendingRequests.values().forEach(future -> 
            future.completeExceptionally(new RuntimeException("Client disconnected")));
        pendingRequests.clear();
        
        logger.info("Disconnected from server");
    }
    
    /**
     * 发送消息
     */
    public CompletableFuture<ClusterMessage> sendMessage(ClusterMessage message) {
        if (!connected) {
            CompletableFuture<ClusterMessage> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Client not connected"));
            return future;
        }
        
        CompletableFuture<ClusterMessage> future = new CompletableFuture<>();
        String requestId = message.getMessageId();
        pendingRequests.put(requestId, future);
        
        try {
            byte[] data = serializer.serialize(message);
            ByteBuf buf = channel.alloc().buffer(data.length);
            buf.writeBytes(data);
            
            channel.writeAndFlush(buf).addListener((ChannelFutureListener) channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    pendingRequests.remove(requestId);
                    future.completeExceptionally(channelFuture.cause());
                }
            });
            
            logger.debug("Sent message: {}", message);
            
        } catch (Exception e) {
            pendingRequests.remove(requestId);
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 发送心跳消息
     */
    public CompletableFuture<ClusterMessage> sendHeartbeat() {
        ClusterMessage heartbeat = new ClusterMessage(ClusterMessage.MessageType.HEARTBEAT, "Heartbeat");
        heartbeat.setSourceNodeId(clientId);
        return sendMessage(heartbeat);
    }
    
    /**
     * 发送数据消息
     */
    public CompletableFuture<ClusterMessage> sendData(String data) {
        ClusterMessage message = new ClusterMessage(ClusterMessage.MessageType.DATA, data);
        message.setSourceNodeId(clientId);
        return sendMessage(message);
    }
    
    /**
     * 发送命令消息
     */
    public CompletableFuture<ClusterMessage> sendCommand(String command) {
        ClusterMessage message = new ClusterMessage(ClusterMessage.MessageType.COMMAND, command);
        message.setSourceNodeId(clientId);
        return sendMessage(message);
    }
    
    /**
     * 发送广播消息
     */
    public CompletableFuture<ClusterMessage> sendBroadcast(String content) {
        ClusterMessage message = new ClusterMessage(ClusterMessage.MessageType.BROADCAST, content);
        message.setSourceNodeId(clientId);
        return sendMessage(message);
    }
    
    /**
     * 注册到服务器
     */
    private void registerToServer() {
        ClusterMessage registerMsg = new ClusterMessage(ClusterMessage.MessageType.REGISTER, clientId);
        registerMsg.setSourceNodeId(clientId);
        registerMsg.addAttribute("version", "1.0.0");
        registerMsg.addAttribute("startTime", System.currentTimeMillis());
        
        sendMessage(registerMsg).whenComplete((response, throwable) -> {
            if (throwable != null) {
                logger.error("Failed to register with server", throwable);
            } else {
                logger.info("Successfully registered with server: {}", response.getContent());
            }
        });
    }
    
    /**
     * 启动心跳任务
     */
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            if (connected && running) {
                sendHeartbeat().whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        logger.warn("Heartbeat failed", throwable);
                        // 心跳失败，可能需要重连
                        if (connected) {
                            logger.info("Heartbeat failed, marking connection as unhealthy");
                        }
                    } else {
                        logger.debug("Heartbeat successful");
                    }
                });
            }
        }, config.getHeartbeatInterval(), config.getHeartbeatInterval(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * 处理服务器响应
     */
    public void handleResponse(ClusterMessage response) {
        String requestId = response.getMessageId();
        CompletableFuture<ClusterMessage> future = pendingRequests.remove(requestId);
        
        if (future != null) {
            if (response.getAttribute("error") != null) {
                future.completeExceptionally(new RuntimeException(response.getContent()));
            } else {
                future.complete(response);
            }
        } else {
            logger.warn("Received response for unknown request: {}", requestId);
        }
    }
    
    /**
     * 关闭客户端
     */
    public void close() {
        logger.info("Closing cluster client");
        
        disconnect();
        
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (group != null) {
            group.shutdownGracefully();
        }
        
        logger.info("Cluster client closed");
    }
    
    // Getters
    public boolean isConnected() {
        return connected;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public Channel getChannel() {
        return channel;
    }
    
    /**
     * 主方法 - 测试客户端
     */
    public static void main(String[] args) {
        try {
            ClusterConfig config = new ClusterConfig();
            ClusterClient client = new ClusterClient(config);
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(client::close));
            
            // 连接到服务器
            client.connect("localhost", 8080).whenComplete((connected, throwable) -> {
                if (throwable != null) {
                    logger.error("Failed to connect", throwable);
                    return;
                }
                
                if (connected) {
                    logger.info("Connected successfully");
                    
                    // 发送一些测试消息
                    client.sendData("Hello from client!").whenComplete((response, error) -> {
                        if (error != null) {
                            logger.error("Failed to send data", error);
                        } else {
                            logger.info("Data sent successfully: {}", response.getContent());
                        }
                    });
                    
                    client.sendCommand("status").whenComplete((response, error) -> {
                        if (error != null) {
                            logger.error("Failed to send command", error);
                        } else {
                            logger.info("Command executed: {}", response.getContent());
                        }
                    });
                }
            });
            
            // 保持运行
            Thread.sleep(Long.MAX_VALUE);
            
        } catch (Exception e) {
            logger.error("Client error", e);
        }
    }
}
