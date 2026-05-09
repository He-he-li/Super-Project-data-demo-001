package com.example.data_demo_002.modules.iot_server.protocol;


import lombok.Data;

/**
 * 设备解析后的数据
 */
@Data
public class DeviceData {
    private String frameHeader;   // 帧头 A55A
    private String deviceId;      // 设备编号
    private String latitude;      // 纬度
    private String longitude;     // 经度
    private String status;        // 设备状态
    private String rawData;       // 原始报文
}