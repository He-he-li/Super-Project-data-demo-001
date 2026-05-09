package com.example.data_demo_002.modules.iot_server.netty;



import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettyTcpServer implements ApplicationRunner {

    public static final int PORT = 3344;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final NettyServerInitializer initializer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(initializer);

            ChannelFuture f = b.bind(PORT).sync();
            log.info("✅ Netty 物联网 TCP 服务启动成功，端口：{}", PORT);
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @PreDestroy
    public void close() {
        log.info("✅ Netty 服务已安全关闭");
    }
}