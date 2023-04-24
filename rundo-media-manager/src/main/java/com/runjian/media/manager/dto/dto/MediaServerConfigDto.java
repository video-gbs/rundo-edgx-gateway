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
    private String serverStarted;
    private String serverKeepalive;
    private String streamChanged;
    private String streamNoneReader;
    private Integer httpPort;
    private String httpIp;
    private String rtpPortRange;
    private Integer keepAliveTTL;
    

}
