package com.runjian.common.commonDto.Gateway.req;

import lombok.Data;

/**
 * 无人观看消费传参
 * @author chenjialing
 */
@Data
public class NoneStreamReaderReq {
    /**
     * 流应用
     */
    private String app;
    /**
     * 流协议 rtsp或rtmp
     */
    private String schema;
    private String streamId;
    private String mediaServerId;
    /**
     * 业务消息id
     */
    private String msgId;
}
