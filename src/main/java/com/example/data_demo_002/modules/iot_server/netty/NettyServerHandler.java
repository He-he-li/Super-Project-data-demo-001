package com.example.data_demo_002.modules.iot_server.netty;



import cn.hutool.core.util.HexUtil;
import com.example.data_demo_002.modules.iot_server.protocol.DeviceData;
import com.example.data_demo_002.modules.iot_server.protocol.ProtocolParser;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<byte[]> {

    // 设备上线
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("✅ 设备上线：{}", ctx.channel().remoteAddress());
    }

    // 设备下线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("❌ 设备下线：{}", ctx.channel().remoteAddress());
    }

    // 接收设备上报数据（16进制报文）
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) {
        String hex = HexUtil.encodeHexStr(msg);
        log.info("📥 设备上报报文：{}", hex);

        DeviceData data = ProtocolParser.parse(hex);

        log.info("✅ 设备上报报文解析成功");
        log.info("✅ 解析后报文：{} ", data);


        // ==============================
        // 在这里写：协议解析 → 数据入库
        // 1. 解析 hex
        // 2. 调用 service 存数据库
        // ==============================
    }

    // 异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("设备通信异常", cause);
        ctx.close();
    }
}