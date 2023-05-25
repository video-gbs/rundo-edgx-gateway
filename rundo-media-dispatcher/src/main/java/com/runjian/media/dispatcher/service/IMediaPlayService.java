package com.runjian.media.dispatcher.service;


import com.runjian.common.commonDto.Gb28181Media.req.CustomPlayReq;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;

/**
 * 点播处理
 * @author chenjialing
 */
public interface IMediaPlayService {


    /**
     * 点播
     * @param mediaPlayReq
     */
    void play(MediaPlayReq mediaPlayReq);

    /**
     * 回放
     * @param mediaPlayBackReq
     */
    void playBack(MediaPlayBackReq mediaPlayBackReq);

    /**
     * 自定义直播
     * @param customPlayReq
     */
    StreamInfo playCustom(CustomPlayReq customPlayReq);
    /**
     * 点播通知
     * @param gatewayStreamNotify
     */
    void streamNotifyServer(GatewayStreamNotify gatewayStreamNotify);

    /**
     * sip成功，但是推流失败的处理
     */
    public void playBusinessErrorScene(StreamBusinessSceneResp businessSceneRespEnd);

}
