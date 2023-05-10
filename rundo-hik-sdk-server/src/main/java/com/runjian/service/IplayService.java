package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.domain.req.PlaySdkReq;

public interface IplayService {

    /**
     * 点播接口处理
     * @param playReq
     */
    public void play(PlaySdkReq playReq);

    /**
     * 点播接口处理
     * @param playBackReq
     */
    public void playBack(PlayBackReq playBackReq);
    /**
     * 流注册事件
     * @param streamInfo
     */
    public void onStreamChanges(StreamInfo streamInfo);

    /**
     * 无人观看处理
     * @param noneStreamReaderReq
     */
    public void onStreamNoneReader(NoneStreamReaderReq noneStreamReaderReq);

    public void playBusinessErrorScene(String businessKey, BusinessSceneResp businessSceneResp);

    /**
     * 网关的bye指令场景
     * @param streamId
     * @param msgId
     */
    Boolean streamBye(String streamId,String msgId);

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
