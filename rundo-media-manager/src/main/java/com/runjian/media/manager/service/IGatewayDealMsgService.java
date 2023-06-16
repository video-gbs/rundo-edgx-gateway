package com.runjian.media.manager.service;

import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.media.manager.dto.entity.OnlineStreamsEntity;

public interface IGatewayDealMsgService {
    /**
     * 网关点播消息通知
     * @param playCommonSsrcInfo
     * @param playReq
     */
    void sendGatewayPlayMsg(SsrcInfo playCommonSsrcInfo, MediaPlayReq playReq);

    /**
     * 网关录播点播消息通知
     * @param playCommonSsrcInfo
     * @param playReq
     */
    void sendGatewayPlayBackMsg(SsrcInfo playCommonSsrcInfo, MediaPlayBackReq playReq);

    /**
     * 网关bye消息通知
     * @param streamId
     * @param msgId
     * @param oneBystreamId
     */
    void sendGatewayStreamBye(String streamId, String msgId, OnlineStreamsEntity oneBystreamId);
}
