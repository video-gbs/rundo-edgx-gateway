package com.runjian.service;

import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.domain.req.PlayReq;

public interface IplayService {

    /**
     * 点播接口处理
     * @param playReq
     */
    public void play(PlayReq playReq);

    /**
     * 流注册事件
     * @param streamInfo
     * @param msgId
     */
    public void onStreamChanges(StreamInfo streamInfo,String msgId);

    public void onStreamNoneReader();

    public void playBusinessErrorScene(String businessKey, BusinessSceneResp businessSceneResp);

    void streamBye(String streamId,String msgId);
}
