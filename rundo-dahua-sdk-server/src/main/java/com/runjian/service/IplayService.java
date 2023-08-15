package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.config.response.CommonResponse;

public interface IplayService {

    /**
     * 点播接口处理
     * @param playReq
     */
    CommonResponse<Integer> play(PlayReq playReq) ;

    /**
     *回放
     * @param playBackReq
     */
    CommonResponse<Integer> playBack(PlayBackReq playBackReq);

    /**
     * 网关的bye指令场景
     * @param streamId
     */
    Boolean streamBye(String streamId);

    /**
     * 录像回放倍速
     * @param streamId
     * @param speed
     */
    Integer playSpeedControl(String streamId,Double speed);

    /**
     * 录像回放暂停
     * @param streamId
     */
    Integer playPauseControl(String streamId);


    /**
     * 录像回放恢复
     * @param streamId
     */
    Integer playResumeControl(String streamId);

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
