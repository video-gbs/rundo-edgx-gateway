package com.runjian.media.dispatcher.dto.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author chenjialing
 */
@Data
public class OnlineStreamsEntity {
    private Long id;

    /**
     * 网关序列号
     */
    private String gatewaySerialnum;
    /**
     * 流媒体id
     */
    private String mediaServerId;
    /**
     * 流id
     */
    private String streamId;

    /**
     * 流应用
     */
    private String app;
    /**
     * 录像状态0，1
     */
    private int recordState;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
