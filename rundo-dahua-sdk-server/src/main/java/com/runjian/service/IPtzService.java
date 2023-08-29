package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.ChannelPtzControlReq;
import com.runjian.common.commonDto.Gateway.req.DragZoomControlReq;
import com.runjian.domain.dto.commder.PresetQueryDto;

/**
 * ptz处理服务
 * @author chenjialing
 */
public interface IPtzService {


    /**
     * 预置位控制
     * @param channelPtzControlReq
     */
    Integer ptzControl(ChannelPtzControlReq channelPtzControlReq);

    /**
     * 预置位查询
     * @param deviceId
     * @param channelId
     * @param msgId
     */

    PresetQueryDto ptzPresetControl(String deviceId, String channelId, String msgId);

    /**
     * 拉框放大缩小
     * @param dragZoomControlReq
     */
    Integer dragZoomControl(DragZoomControlReq dragZoomControlReq);
}
