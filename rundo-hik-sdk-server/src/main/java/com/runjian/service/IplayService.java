package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.domain.req.PlaySdkReq;

public interface IplayService {

    /**
     * 点播接口处理
     * @param playReq
     */
    CommonResponse<Integer> play(PlayReq playReq);

    /**
     *回放
     * @param playBackReq
     */
    void playBack(PlayBackReq playBackReq);

    /**
     * 网关的bye指令场景
     * @param streamId
     * @param msgId
     */
    Boolean streamBye(String streamId);

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

    /**
     * 重启修改全部流状态
     */
    void restartStopAll();
}
