package com.runjian.media.manager.dto.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author chenjialing
 */
@Data
@TableName("rundo_media_server")
public class MediaServerEntity {

    /**
     * ID
     */
    private String id;

    /**
     *    @Schema(description = "IP")
     */
    private String ip;
    /**
     *    @Schema(description = "hook使用的IP（zlm访问WVP使用的IP）")
     */
    private String hookIp;

    /**
     *    @Schema(description = "SDP IP")
     */
    private String sdpIp;

    /**
     * @Schema(description = "流IP")
     */
    private String streamIp;
    /**
     *    @Schema(description = "HTTP端口")
     */
    private int httpPort;
    /**
     *    @Schema(description = "HTTPS端口")
     */
    private int httpSslPort;
    /**
     *http播放端口--针对docker中内外网段端口不一致
     */
    private int httpPlayPort;

    /**
     * 是否启用https
     */
    private int enableHttps;
    /**
     *    @Schema(description = "RTMP端口")
     */
    private int rtmpPort;
    /**
     *    @Schema(description = "RTMPS端口")
     */
    private int rtmpSslPort;
    /**
     *    @Schema(description = "RTP收流端口（单端口模式有用）")
     */
    private int rtpProxyPort;
    /**
     *    @Schema(description = "RTSP端口")
     */
    private int rtspPort;
    /**
     *    @Schema(description = "RTSPS端口")
     */
    private int rtspSslPort;

    /**
     *    @Schema(description = "ZLM鉴权参数")
     */
    private String secret;


    /**
     *    @Schema(description = "是否使用多端口模式")
     */
    private boolean rtpEnable;

    /**
     *    @Schema(description = "状态")
     */
    private int online;

    /**
     *    @Schema(description = "多端口RTP收流端口范围")
     */
    private String rtpPortRange;
    /**
     *    @Schema(description = "RTP发流端口范围")
     */
    private String sendRtpPortRange;


    /**
     * 是否是默认流媒体
     */
    private boolean defaultServer;

    /**
     *    @Schema(description = "创建时间")
     */
    private String createdAt;

    /**
     *    @Schema(description = "更新时间")
     */
    private String updatedAt;



}
