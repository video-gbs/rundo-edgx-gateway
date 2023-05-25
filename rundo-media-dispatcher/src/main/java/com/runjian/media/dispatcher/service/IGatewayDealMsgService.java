package com.runjian.media.dispatcher.service;

import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.SsrcInfo;

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


    void sendGatewayStreamBye(String streamId, String msgId, BaseRtpServerDto baseRtpServerDto);
}
