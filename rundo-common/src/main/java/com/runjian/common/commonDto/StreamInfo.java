package com.runjian.common.commonDto;

import lombok.Data;

@Data
public class StreamInfo {


    private String app;

    private String stream;

    private String deviceID;

    private String channelId;

    private String flv;


    private String ip;

    private String https_flv;

    private String ws_flv;

    private String wss_flv;

    private String fmp4;

    private String https_fmp4;

    private String ws_fmp4;

    private String wss_fmp4;

    private String hls;

    private String https_hls;

    private String ws_hls;

    private String wss_hls;

    private String ts;

    private String https_ts;

    private String ws_ts;

    private String wss_ts;

    private String rtmp;

    private String rtmps;

    private String rtsp;

    private String rtsps;

    private String rtc;


    private String rtcs;

    private String mediaServerId;

    private Object tracks;

    private String startTime;

    private String endTime;

    private double progress;


    private boolean pause;

    public static class TransactionInfo{
        public String callId;
        public String localTag;
        public String remoteTag;
        public String branch;
    }

}
