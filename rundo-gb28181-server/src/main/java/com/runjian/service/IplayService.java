package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.commonDto.Gateway.req.PlayReq;

public interface IplayService {

    /**
     * 点播接口处理
     * @param playReq
     */
    public void play(PlayReq playReq);

    /**
     * 点播接口处理
     * @param playBackReq
     */
    public void playBack(PlayBackReq playBackReq);
    /**
     * 流注册事件
     * @param streamInfo
     * @param msgId
     */
    public void onStreamChanges(StreamInfo streamInfo,String msgId);

    /**
     * 无人观看处理
     * @param noneStreamReaderReq
     */
    public void onStreamNoneReader(NoneStreamReaderReq noneStreamReaderReq);

    public void playBusinessErrorScene(String businessKey, BusinessSceneResp businessSceneResp);

    void streamBye(String streamId,String msgId);
}
