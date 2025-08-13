package com.example.nettycluster.server;

import com.example.nettycluster.config.ClusterConfig;
import com.example.nettycluster.manager.ClusterManager;
import com.example.nettycluster.discovery.SimpleDiscoveryService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Netty集群服务器
 * 集成Spring Boot，支持自动启动和停止
 */
@Component
public class ClusterServer {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterServer.class);
    
    private final ClusterConfig config;
    private final ClusterManager clusterManager;
    private final SimpleDiscoveryService discoveryService;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean running = false;
    
    @Autowired
    public ClusterServer(ClusterConfig config, ClusterManager clusterManager, SimpleDiscoveryService discoveryService) {
        this.config = config;
        this.clusterManager = clusterManager;
        this.discoveryService = discoveryService;
    }
    
    /**
     * Spring Boot启动后自动启动Netty服务器
     */
    @PostConstruct
    public void start() {
        try {
            logger.info("正在启动Netty集群服务器...");
            
            // 启动集群管理器
            clusterManager.start();
            
            // 注册到服务发现
            discoveryService.registerLocalNode();
            
            // 启动Netty服务器
            startNettyServer();
            
            running = true;
            logger.info("Netty集群服务器启动成功，监听端口: {}", config.getServerPort());
            
        } catch (Exception e) {
            logger.error("Netty集群服务器启动失败", e);
            throw new RuntimeException("服务器启动失败", e);
        }
    }
    
    /**
     * Spring Boot关闭前自动停止Netty服务器
     */
    @PreDestroy
    public void stop() {
        try {
            logger.info("正在停止Netty集群服务器...");
            
            // 停止Netty服务器
            stopNettyServer();
            
            // 从服务发现注销
            discoveryService.deregisterLocalNode();
            
            // 停止集群管理器
            clusterManager.stop();
            
            running = false;
            logger.info("Netty集群服务器已停止");
            
        } catch (Exception e) {
            logger.error("停止Netty集群服务器时发生错误", e);
        }
    }
    
    /**
     * 启动Netty服务器
     */
    private void startNettyServer() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(config.getBossThreads());
        workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new ClusterServerHandler(clusterManager));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);
            
            ChannelFuture future = bootstrap.bind(config.getServerPort()).sync();
            serverChannel = future.channel();
            
            logger.info("Netty服务器绑定到端口: {}", config.getServerPort());
            
        } catch (Exception e) {
            logger.error("启动Netty服务器失败", e);
            throw e;
        }
    }
    
    /**
     * 停止Netty服务器
     */
    private void stopNettyServer() {
        if (serverChannel != null) {
            serverChannel.close();
            serverChannel = null;
        }
        
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }
    
    /**
     * 检查服务器是否正在运行
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 获取服务器端口
     */
    public int getPort() {
        return config.getServerPort();
    }
    
    /**
     * 获取集群管理器
     */
    public ClusterManager getClusterManager() {
        return clusterManager;
    }
    
    /**
     * 优雅关闭
     */
    public void shutdown() {
        stop();
    }
}
