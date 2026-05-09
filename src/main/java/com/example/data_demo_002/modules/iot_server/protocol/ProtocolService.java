package com.example.data_demo_002.modules.iot_server.protocol;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备数据解析服务
 * 给 Netty 调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolService {

    /**
     * 解析并处理设备上报数据
     */
    public void processDeviceData(String hexData) {
        // 1. 解析
        DeviceData data = ProtocolParser.parse(hexData);

        // 2. 打印解析结果（你最想看的！）
        log.info("==================================");
        log.info("✅ 设备解析完成");
        log.info("📶 帧头：" + data.getFrameHeader());
        log.info("📶 设备编号：" + data.getDeviceId());
        log.info("🌍 纬度：" + data.getLatitude());
        log.info("🌍 经度：" + data.getLongitude());
        log.info("==================================");

        // 3. 这里可以继续存数据库
        // deviceService.save(data);
    }
}