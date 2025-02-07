package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.commonDto.StreamPlayDto;
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
     */
    @Deprecated
    public void onStreamChanges(StreamInfo streamInfo);



    @Deprecated
    public void playBusinessErrorScene(String businessKey, BusinessSceneResp businessSceneResp);

    /**
     * 网关的bye指令场景
     * @param streamId
     * @param msgId
     */
    void streamBye(StreamPlayDto streamPlayDto, String msgId);


    /**
     * 网关的bye指令场景
     * @param streamId
     * @param callId
     */
    Boolean testStreamBye(String streamId,String callId);
    /**
     * 录像回放倍速
     * @param streamId
     * @param speed
     * @param msgId
     */
    void playSpeedControl(String streamId,Double speed,String msgId);

    /**
     * 录像回放暂停
     * @param streamId
     * @param msgId
     */
    void playPauseControl(String streamId,String msgId);


    /**
     * 录像回放恢复
     * @param streamId
     * @param msgId
     */
    void playResumeControl(String streamId,String msgId);

    /**
     * 录像回放拖拉
     * @param streamId
     * @param seekTime 拖拉的时间
     * @param msgId
     */
    void playSeekControl(String streamId,long seekTime,String msgId);
}
