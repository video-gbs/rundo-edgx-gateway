package com.runjian.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author chenjialing
 */
@Data
public class DeviceDto {
    /**
     * 数据id
     */
    private long id;
    /**
     * 设备国标编号
     */
    @Schema(description = "设备国标编号")
    private String deviceId;

    /**
     * 设备名
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 生产厂商
     */
    @Schema(description = "生产厂商")
    private String manufacturer;

    /**
     * 型号
     */
    @Schema(description = "型号")
    private String model;

    /**
     * 固件版本
     */
    @Schema(description = "固件版本")
    private String firmware;

    /**
     * 传输协议
     * UDP/TCP
     */
    @Schema(description = "传输协议（UDP/TCP）")
    private String transport;

    /**
     * 数据流传输模式
     * UDP:udp传输
     * TCP-ACTIVE：tcp主动模式
     * TCP-PASSIVE：tcp被动模式
     */
    @Schema(description = "数据流传输模式")
    private String streamMode;

    /**
     * wan地址_ip
     */
    @Schema(description = "IP")
    private String  ip;

    /**
     * wan地址_port
     */
    @Schema(description = "端口")
    private int port;

    /**
     * wan地址
     */
    @Schema(description = "wan地址")
    private String  hostAddress;

    /**
     * 在线
     */
    @Schema(description = "是否在线，1为在线，0为离线")
    private int online;

    /**
     * 注册时间
     */
    @Schema(description = "注册时间")
    private String registerTime;


    /**
     * 心跳时间
     */
    @Schema(description = "心跳时间")
    private String keepaliveTime;

    /**
     * 注册有效期
     */
    @Schema(description = "心跳有效期")
    private long keepaliveIntervalTime;

    /**
     * 注册有效期
     */
    @Schema(description = "注册有效期")
    private int expires;

    /**
     * 字符集, 支持 UTF-8 与 GB2312
     */
    @Schema(description = "符集, 支持 UTF-8 与 GB2312")
    private String charset ;
    /**
     * 是否开启ssrc校验，默认关闭，开启可以防止串流
     */
    @Schema(description = "是否开启ssrc校验，默认关闭，开启可以防止串流")
    private boolean ssrcCheck = true;

    @Schema(description = "密码")
    private String password;

    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;
}
