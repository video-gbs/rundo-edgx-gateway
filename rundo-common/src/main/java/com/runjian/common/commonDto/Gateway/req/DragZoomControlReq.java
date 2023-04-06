package com.runjian.common.commonDto.Gateway.req;

import lombok.Data;

/**
 * ptz/设备控制操作请求指令
 * @author chenjialing
 */
@Data
public class DragZoomControlReq {
    private String deviceId;
    private String channelId;
    /**
     *    播放窗口长度像素值
     */
    private int length;
    /**
     *播放窗口宽度像素值
     */
    private int width;

    /**
     *拉框中心的横轴坐标像素值
     */
    private int midpointx;

    /**
     * 拉框中心的纵轴坐标像素值
     */
    private int midpointy;
    /**
     *拉框长度像素值
     */
    private int lengthx;
    /**
     *拉框宽度像素值
     */
    private int lengthy;

    /**
     * 拉框放大和缩小
     */
    private Integer dragOperationType;

    /**
     * 业务消息id
     */
    String msgId;
}
