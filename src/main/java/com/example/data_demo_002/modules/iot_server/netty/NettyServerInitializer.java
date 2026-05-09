package com.example.data_demo_002.modules.iot_server.netty;



import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyServerHandler serverHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast("decoder", new ByteArrayDecoder())
                .addLast("encoder", new ByteArrayEncoder())
                .addLast("handler", serverHandler);
    }
}