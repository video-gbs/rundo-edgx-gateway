package com.runjian.entity;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class PlayBackToolEntity {

    /**
     * 主子码流：，0主码流
     */
    private long bitStreamId;
    /**
     * 通道号
     */
    private long channelNum;
    /**
     * 设备ip
     */
    private String deviceIp;
    /**
     * 登陆密码
     */
    private String devicepassword;
    /**
     * 设备端口
     */
    private long devicePort;
    /**
     * 设备登陆用户
     */
    private String deviceUser;
    /**
     * 连接方式，0：TCP方式,1：UDP方式,2：多播方式,3 - RTP方式，4-RTP/RTSP,5-RSTP/HTTP ,6- HRUDP（可靠传输） ,7-RTSP/HTTPS
     */
    private long linkMode;
    /**
     * 流媒体端口
     */
    private long mediaGb28181Port;
    /**
     * 流媒体ip
     */
    private String mediaIp;
    /**
     * 流传输模式，1:tcp,2udp
     */
    private long streamMode;

    /**
     * 开始时间
     */
    String startTime;
    /**
     * 结束时间
     */
    String endTime;

}
