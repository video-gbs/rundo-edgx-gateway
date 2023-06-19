package com.runjian.media.manager.dto.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author chenjialing
 */
@Data
public class OnlineStreamsEntity {


    @NotNull(message = "流id不得为null")
    private String streamId;

    /**
     * 流操作句柄
     */
    private Integer key;

    @NotNull(message = "设备id不得为null")
    private String deviceId;

    @NotNull(message = "通道id不得为null")
    private String channelId;

    private String app;
    /**
     * 流传输模式
     */
    String streamMode;
    /**
     * ssrc的参数
     */
    private String ssrc;

    /**
     * 自定义端口
     */
    private Integer port;

    /**
     * 默认不开启音频
     */
    private Boolean enableAudio = false;

    /**
     * 默认进行ssrc校验
     */
    private Boolean ssrcCheck = true;
    /**
     * 录像状态
     */
    private int recordState;

    /**
     * 0准备中，1流在线状态
     */
    private int status;

    /**
     * 流媒体id
     */
    private String mediaServerId;


    @NotNull(message = "网关的id")
    private String gatewayId;

    @NotNull(message = "交换机信息")
    private String mqExchange;

    @NotNull(message = "mq的路由")
    private String mqRouteKey;

    @NotNull(message = "mq的队列名称")
    private String mqQueueName;

    /**
     * 调度服务地址
     */
    private String dispatchUrl;

    /**
     * 0点播，1.自定义推流
     */
    private int streamType;
}
