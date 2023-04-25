package com.runjian.service.impl;

import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class PlayServiceImpl implements IplayService {
    @Override
    public void play(PlayReq playReq) {

    }

    @Override
    public void playBack(PlayBackReq playBackReq) {

    }

    @Override
    public void onStreamChanges(StreamInfo streamInfo) {

    }

    @Override
    public void onStreamNoneReader(NoneStreamReaderReq noneStreamReaderReq) {

    }

    @Override
    public void playBusinessErrorScene(String businessKey, BusinessSceneResp businessSceneResp) {

    }

    @Override
    public void streamBye(String streamId, String msgId) {

    }

    @Override
    public void playSpeedControl(String streamId, Double speed, String msgId) {

    }

    @Override
    public void playPauseControl(String streamId, String msgId) {

    }

    @Override
    public void playResumeControl(String streamId, String msgId) {

    }

    @Override
    public void playSeekControl(String streamId, long seekTime, String msgId) {

    }
}
