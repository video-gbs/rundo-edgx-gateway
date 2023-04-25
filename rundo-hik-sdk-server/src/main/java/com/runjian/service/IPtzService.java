package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.ChannelPtzControlReq;
import com.runjian.common.commonDto.Gateway.req.DeviceControlReq;
import com.runjian.common.commonDto.Gateway.req.DragZoomControlReq;

/**
 * ptz处理服务
 * @author chenjialing
 */
public interface IPtzService {


    /**
     * 预置位控制
     * @param channelPtzControlReq
     */
    void ptzControl(ChannelPtzControlReq channelPtzControlReq);

    /**
     * 预置位查询
     * @param deviceId
     * @param channelId
     * @param msgId
     */

    void ptzPresetControl(String deviceId, String channelId,String msgId);

    /**
     * 拉框放大缩小
     * @param dragZoomControlReq
     */
    void dragZoomControl(DragZoomControlReq dragZoomControlReq);
}
