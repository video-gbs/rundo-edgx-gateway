package com.runjian.media.manager.dto.dto;


import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class MediaServerConfigDto {

    private Integer  apiDebug = 1;
    private Integer streamNoneReaderDelayMS;
    private String schedulerIp;
    private Integer schedulerPort;
    private String mediaServerId;
    private Integer msgPushEnable;

    private String onRegisterMediaNode;
    private String onUnregisterMediaNode;
    private String onServerKeepalive;
    private String streamChanged;
    private String onStreamNoneReader;
    private String onStreamArrive;
    private String onStreamNoneArrive;
    private String onStreamDisconnect;
    private String onStreamNotFound;
    private String onPublish;
    private Integer httpPort;
    private String httpIp;
    private String rtpPortRange;
    private String sdkPortRange;


}
