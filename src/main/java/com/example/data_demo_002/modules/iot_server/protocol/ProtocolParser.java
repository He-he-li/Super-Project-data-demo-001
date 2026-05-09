package com.example.data_demo_002.modules.iot_server.protocol;




/**
 * 协议解析器
 * 专门解析你设备上传的 a55a 开头的报文
 */


import cn.hutool.core.util.HexUtil;

public class ProtocolParser {

    public static DeviceData parse(String hex) {
        DeviceData data = new DeviceData();
        data.setRawData(hex);

        // ======================== 固定正确截取 ========================
        // 帧头 4
        data.setFrameHeader(hex.substring(0, 4));

        // 设备编号 16进制字符串 28
        data.setDeviceId(hex.substring(8, 36));

        // 纬度 8字节 = 16个字符
        String latHex = hex.substring(50, 66);
        long latLong = Long.parseLong(latHex, 16);
        double lat = latLong / 1000000.0;
        data.setLatitude(String.format("%.6f", lat));

        // 经度 4字节 = 8个字符
        String lngHex = hex.substring(66, 74);
        long lngLong = Long.parseLong(lngHex, 16);
        double lng = lngLong / 1000000.0;
        data.setLongitude(String.format("%.6f", lng));

        return data;
    }
}