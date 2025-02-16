package com.runjian.domain.dto;


import com.runjian.conf.SsrcConfig;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;

@Schema(description = "流媒体服务信息")
public class MediaServerItem {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "IP")
    private String ip;

    @Schema(description = "hook使用的IP（zlm访问WVP使用的IP）")
    private String hookIp;

    @Schema(description = "SDP IP")
    private String sdpIp;

    @Schema(description = "流IP")
    private String streamIp;

    @Schema(description = "HTTP端口")
    private int httpPort;

    @Schema(description = "HTTPS端口")
    private int httpSSlPort;
    /**
     *http播放端口--针对docker中内外网段端口不一致
     */
    private int httpPlayPort;

    /**
     * 是否启用https
     */
    private int enableHttps;

    @Schema(description = "RTMP端口")
    private int rtmpPort;

    @Schema(description = "RTMPS端口")
    private int rtmpSSlPort;

    @Schema(description = "RTP收流端口（单端口模式有用）")
    private int rtpProxyPort;

    @Schema(description = "RTSP端口")
    private int rtspPort;

    @Schema(description = "RTSPS端口")
    private int rtspSSLPort;

    @Schema(description = "是否开启自动配置ZLM")
    private boolean autoConfig;

    @Schema(description = "ZLM鉴权参数")
    private String secret;

    @Schema(description = "keepalive hook触发间隔,单位秒")
    private int hookAliveInterval;

    @Schema(description = "是否使用多端口模式")
    private boolean rtpEnable;

    @Schema(description = "状态")
    private boolean status;

    @Schema(description = "多端口RTP收流端口范围")
    private String rtpPortRange;

    @Schema(description = "RTP发流端口范围")
    private String sendRtpPortRange;

    @Schema(description = "assist服务端口")
    private int recordAssistPort;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description = "上次心跳时间")
    private String lastKeepaliveTime;

    @Schema(description = "是否是默认ZLM")
    private boolean defaultServer;

    @Schema(description = "SSRC信息")
    private SsrcConfig ssrcConfig;

    @Schema(description = "当前使用到的端口")
    private int currentPort;


    /**
     * 每一台ZLM都有一套独立的SSRC列表
     * 在ApplicationCheckRunner里对mediaServerSsrcMap进行初始化
     */
    @Schema(description = "ID")
    private HashMap<String, SsrcConfig> mediaServerSsrcMap;

    public MediaServerItem() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHookIp() {
        return hookIp;
    }

    public void setHookIp(String hookIp) {
        this.hookIp = hookIp;
    }

    public String getSdpIp() {
        return sdpIp;
    }

    public void setSdpIp(String sdpIp) {
        this.sdpIp = sdpIp;
    }

    public String getStreamIp() {
        return streamIp;
    }

    public void setStreamIp(String streamIp) {
        this.streamIp = streamIp;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpSSlPort() {
        return httpSSlPort;
    }

    public void setHttpSSlPort(int httpSSlPort) {
        this.httpSSlPort = httpSSlPort;
    }

    public int getRtmpPort() {
        return rtmpPort;
    }

    public void setRtmpPort(int rtmpPort) {
        this.rtmpPort = rtmpPort;
    }

    public int getRtmpSSlPort() {
        return rtmpSSlPort;
    }

    public void setRtmpSSlPort(int rtmpSSlPort) {
        this.rtmpSSlPort = rtmpSSlPort;
    }

    public int getRtpProxyPort() {
        return rtpProxyPort;
    }

    public void setRtpProxyPort(int rtpProxyPort) {
        this.rtpProxyPort = rtpProxyPort;
    }

    public int getRtspPort() {
        return rtspPort;
    }

    public void setRtspPort(int rtspPort) {
        this.rtspPort = rtspPort;
    }

    public int getRtspSSLPort() {
        return rtspSSLPort;
    }

    public void setRtspSSLPort(int rtspSSLPort) {
        this.rtspSSLPort = rtspSSLPort;
    }

    public boolean isAutoConfig() {
        return autoConfig;
    }

    public void setAutoConfig(boolean autoConfig) {
        this.autoConfig = autoConfig;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isRtpEnable() {
        return rtpEnable;
    }

    public void setRtpEnable(boolean rtpEnable) {
        this.rtpEnable = rtpEnable;
    }

    public String getRtpPortRange() {
        return rtpPortRange;
    }

    public void setRtpPortRange(String rtpPortRange) {
        this.rtpPortRange = rtpPortRange;
    }

    public int getRecordAssistPort() {
        return recordAssistPort;
    }

    public void setRecordAssistPort(int recordAssistPort) {
        this.recordAssistPort = recordAssistPort;
    }

    public boolean isDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(boolean defaultServer) {
        this.defaultServer = defaultServer;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public HashMap<String, SsrcConfig> getMediaServerSsrcMap() {
        return mediaServerSsrcMap;
    }

    public void setMediaServerSsrcMap(HashMap<String, SsrcConfig> mediaServerSsrcMap) {
        this.mediaServerSsrcMap = mediaServerSsrcMap;
    }

    public SsrcConfig getSsrcConfig() {
        return ssrcConfig;
    }

    public void setSsrcConfig(SsrcConfig ssrcConfig) {
        this.ssrcConfig = ssrcConfig;
    }

    public int getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(int currentPort) {
        this.currentPort = currentPort;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getLastKeepaliveTime() {
        return lastKeepaliveTime;
    }

    public void setLastKeepaliveTime(String lastKeepaliveTime) {
        this.lastKeepaliveTime = lastKeepaliveTime;
    }

    public String getSendRtpPortRange() {
        return sendRtpPortRange;
    }

    public void setSendRtpPortRange(String sendRtpPortRange) {
        this.sendRtpPortRange = sendRtpPortRange;
    }

    public int getHookAliveInterval() {
        return hookAliveInterval;
    }

    public void setHookAliveInterval(int hookAliveInterval) {
        this.hookAliveInterval = hookAliveInterval;
    }

    public int getEnableHttps() {
        return enableHttps;
    }

    public void setEnableHttps(int enableHttps) {
        this.enableHttps = enableHttps;
    }

    public int getHttpPlayPort() {
        return httpPlayPort;
    }

    public void setHttpPlayPort(int httpPlayPort) {
        this.httpPlayPort = httpPlayPort;
    }
}
