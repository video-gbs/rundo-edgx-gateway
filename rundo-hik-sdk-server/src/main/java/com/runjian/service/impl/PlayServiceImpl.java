package com.runjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.domain.dto.commder.PlayInfoDto;
import com.runjian.domain.req.PlaySdkReq;
import com.runjian.entity.PlayListLogEntity;
import com.runjian.hik.module.service.ISdkCommderService;
import com.runjian.mapper.PlayListLogMapper;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class PlayServiceImpl implements IplayService {
    @Autowired
    ISdkCommderService iSdkCommderService;

    @Autowired
    PlayListLogMapper playListLogMapper;
    @Override
    public void play(PlaySdkReq playReq) {

        PlayInfoDto play = iSdkCommderService.play(playReq.getLUserId(), playReq.getChannelNum(), 1, 1);
        int errorCode = play.getErrorCode();
        int playStatus = errorCode==0?0:-1;
        PlayListLogEntity playListLogEntity = new PlayListLogEntity();
        playListLogEntity.setStreamId(playReq.getStreamId());
        playListLogEntity.setPlayErrorCode(errorCode);
        playListLogEntity.setPlayHandle(play.getLPreviewHandle());
        playListLogEntity.setPlayStatus(playStatus);
        playListLogMapper.insert(playListLogEntity);

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
    public Boolean streamBye(String streamId, String msgId) {
        LambdaQueryWrapper<PlayListLogEntity> playListLogEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        playListLogEntityLambdaQueryWrapper.eq(PlayListLogEntity::getPlayStatus,0);
        playListLogEntityLambdaQueryWrapper.eq(PlayListLogEntity::getStreamId,streamId).last("limit 1");

        PlayListLogEntity playListLogEntity = playListLogMapper.selectOne(playListLogEntityLambdaQueryWrapper);
        PlayInfoDto playInfoDto = iSdkCommderService.stopPlay(playListLogEntity.getPlayHandle());
        int errorCode = playInfoDto.getErrorCode();
        int playStatus = errorCode != 0?1:2;
        playListLogEntity.setPlayStatus(playStatus);
        playListLogEntity.setPlayErrorCode(errorCode);
        playListLogMapper.updateById(playListLogEntity);
        return errorCode == 0;

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
