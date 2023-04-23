package com.runjian.media.manager.dto.dto;


import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class MediaServerConfigDto {

    private int  apiDebug = 1;
    private int streamNoneReaderDelayMS;
    private String schedulerIp;
    private int schedulerPort;
    private String mediaServerId;
    private int msgPushEnable;
    private String serverStarted;
    private String serverKeepalive;
    private String streamChanged;
    private String streamNoneReader;
    private int httpPort;
    private String httpIp;
    private String rtpPortRange;
    private int keepAliveTTL;


}
