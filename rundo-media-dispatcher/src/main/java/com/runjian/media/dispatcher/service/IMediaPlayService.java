package com.runjian.media.dispatcher.service;


import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.config.response.BusinessSceneResp;

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
     * 点播通知
     * @param gatewayStreamNotify
     */
    void streamNotifyServer(GatewayStreamNotify gatewayStreamNotify);

    /**
     * sip成功，但是推流失败的处理
     * @param businessKey
     * @param businessSceneResp
     */
    public void playBusinessErrorScene(String businessKey, BusinessSceneResp businessSceneResp);

}
