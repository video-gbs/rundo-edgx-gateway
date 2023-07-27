package com.runjian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.common.utils.UuidUtil;
import com.runjian.conf.PlayHandleConf;
import com.runjian.domain.dto.PlayCommonDto;
import com.runjian.domain.dto.commder.DeviceLoginDto;
import com.runjian.domain.dto.commder.PlayInfoDto;
import com.runjian.domain.req.PlaySdkReq;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.entity.DeviceEntity;
import com.runjian.entity.PlayListLogEntity;
import com.runjian.hik.module.service.ISdkCommderService;
import com.runjian.hik.sdklib.HCNetSDK;
import com.runjian.hik.sdklib.SocketPointer;
import com.runjian.mapper.PlayListLogMapper;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IplayService;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class PlayServiceImpl implements IplayService {
    @Autowired
    ISdkCommderService iSdkCommderService;

    @Resource
    PlayListLogMapper playListLogMapper;

    @Autowired
    IDeviceService deviceService;

    @Autowired
    IDeviceChannelService deviceChannelService;

    @Value("${mdeia-tool-uri-list.server-ip}")
    private String serverIp;

    @Value("${mdeia-tool-uri-list.open-sdk-server}")
    private String openSdkServerApi;

    @Value("${mdeia-tool-uri-list.close-sdk-server}")
    private String closeSdkServerApi;

    @Autowired
    private PlayHandleConf playHandleConf;

    @Autowired
    RestTemplate restTemplate;

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private ConcurrentHashMap<String,Object> stringSocketHanderMap = new ConcurrentHashMap();

    @Override
    public CommonResponse<Integer> play(PlayReq playReq)  {
        CommonResponse<PlayCommonDto> commonResponse = playCommonCheck(playReq);
        //建立socke连接
        CommonResponse<SocketPointer> commonResponse1 = streamMediaDeal(playReq);
        if(commonResponse1.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
            throw new BusinessException(BusinessErrorEnums.MEDIA_SERVER_SOCKET_ERROR);
        }
        SocketPointer socketPointer = commonResponse1.getData();
        String socketHandle = socketPointer.socketHandle;
        PlayCommonDto data = commonResponse.getData();
        int streamMode = playReq.getStreamMode();
        PlayInfoDto play = iSdkCommderService.play(data.getLUserId(), data.getChannelNum(), streamMode, 1,socketPointer);
        int errorCode = play.getErrorCode();

        int playStatus = errorCode==0?0:-1;
        PlayListLogEntity playListLogEntity = new PlayListLogEntity();
        playListLogEntity.setStreamId(playReq.getStreamId());
        playListLogEntity.setPlayErrorCode(errorCode);
        playListLogEntity.setPlayHandle(play.getLPreviewHandle());
        playListLogEntity.setPlayStatus(playStatus);
        playListLogEntity.setSocketHandle(socketHandle);
        playListLogMapper.insert(playListLogEntity);
        return errorCode==0?CommonResponse.success(errorCode):CommonResponse.failure(BusinessErrorEnums.SDK_OPERATION_FAILURE,BusinessErrorEnums.SDK_OPERATION_FAILURE.getErrMsg()+errorCode);

    }

    private synchronized CommonResponse<SocketPointer> streamMediaDeal(PlayReq playReq) {
        String ip = playReq.getSsrcInfo().getIp();
        int port = playReq.getSsrcInfo().getPort();
        String socketHandle =  UuidUtil.toUuid();
        SocketPointer socketPointer = new SocketPointer();
        socketPointer.socketHandle = socketHandle;
        socketPointer.write();
        Pointer pUser = socketPointer.getPointer();

        try {
            Socket socket = new Socket(ip, port);
            ConcurrentHashMap<Pointer, Object> socketHanderMap = playHandleConf.getSocketHanderMap();
            socketHanderMap.put(pUser,socket);
            stringSocketHanderMap.put(socketHandle,socket);

        }catch (Exception e){
            return CommonResponse.failure(BusinessErrorEnums.MEDIA_SERVER_SOCKET_ERROR);
        }
        return CommonResponse.success(socketPointer);

    }


    private CommonResponse<PlayCommonDto> playCommonCheck(PlayReq playReq){
        //获取设备信息luserId
        long encodeId = Long.parseLong(playReq.getDeviceId());
        DeviceEntity deviceEntity = deviceService.getById(encodeId);
        if(ObjectUtils.isEmpty(deviceEntity)){

        }else {
            if(deviceEntity.getOnline() != 1){
                throw new BusinessException(BusinessErrorEnums.DB_DEVICE_NOT_FOUND);
            }
        }
        DeviceLoginDto login = iSdkCommderService.login(deviceEntity.getIp(), deviceEntity.getPort(), deviceEntity.getUsername(), deviceEntity.getPassword());
        if(login.getErrorCode() != 0){
            throw new BusinessException(BusinessErrorEnums.DEVICE_LOGIN_ERROR);
        }
        int lUserId = login.getLUserId();
        //获取通道信息

        long channelId = Long.parseLong(playReq.getChannelId());
        DeviceChannelEntity deviceChannelEntity = deviceChannelService.getById(channelId);
        if(ObjectUtils.isEmpty(deviceChannelEntity)){
            throw new BusinessException(BusinessErrorEnums.DB_CHANNEL_NOT_FOUND);
        }else {
            if(deviceChannelEntity.getOnline() != 1){
                throw new BusinessException(BusinessErrorEnums.CHANNEL_OFFLINE);

            }
        }

        PlayCommonDto playCommonDto = new PlayCommonDto();
        playCommonDto.setLUserId(lUserId);
        playCommonDto.setChannelNum(deviceChannelEntity.getChannelNum());
        return CommonResponse.success(playCommonDto);

    }
    @Override
    public CommonResponse<Integer> playBack(PlayBackReq playBackReq) {
        CommonResponse<PlayCommonDto> commonResponse = playCommonCheck(playBackReq);
        //建立socke连接
        CommonResponse<SocketPointer> commonResponse1 = streamMediaDeal(playBackReq);
        if(commonResponse1.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
            throw new BusinessException(BusinessErrorEnums.MEDIA_SERVER_SOCKET_ERROR);
        }
        SocketPointer socketPointer = commonResponse1.getData();
        String socketHandle = socketPointer.socketHandle;
        PlayCommonDto data = commonResponse.getData();
        PlayInfoDto play = iSdkCommderService.playBack(data.getLUserId(), data.getChannelNum(), playBackReq.getStartTime(), playBackReq.getEndTime(),socketPointer);
        int errorCode = play.getErrorCode();

        int playStatus = errorCode==0?0:-1;
        PlayListLogEntity playListLogEntity = new PlayListLogEntity();
        playListLogEntity.setStreamId(playBackReq.getStreamId());
        playListLogEntity.setPlayErrorCode(errorCode);
        playListLogEntity.setPlayHandle(play.getLPreviewHandle());
        playListLogEntity.setPlayStatus(playStatus);
        playListLogEntity.setSocketHandle(socketHandle);
        playListLogMapper.insert(playListLogEntity);
        return errorCode==0?CommonResponse.success(errorCode):CommonResponse.failure(BusinessErrorEnums.SDK_OPERATION_FAILURE,BusinessErrorEnums.SDK_OPERATION_FAILURE.getErrMsg()+errorCode);
    }




    @Override
    public Boolean streamBye(String streamId) {
        log.info(LogTemplate.ERROR_LOG_TEMPLATE,"流bye操作","bye进入",streamId);
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
        //关闭流传输
        try{
            Socket socket = (Socket)stringSocketHanderMap.get(playListLogEntity.getSocketHandle());
            socket.shutdownOutput();
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流bye操作","关闭socket失败",e.getMessage());
        }

        return errorCode == 0;

    }

    @Override
    public Integer  playSpeedControl(String streamId, Double speed) {
        PlayListLogEntity playListLogEntity = playControlCommon(streamId);
        Integer playHandle = playListLogEntity.getPlayHandle();
        return iSdkCommderService.playBackControl(playHandle, HCNetSDK.NET_DVR_PLAYFAST, speed.intValue());
    }

    @Override
    public Integer playPauseControl(String streamId) {
        PlayListLogEntity playListLogEntity = playControlCommon(streamId);
        Integer playHandle = playListLogEntity.getPlayHandle();
        return iSdkCommderService.playBackControl(playHandle, HCNetSDK.NET_DVR_PLAYPAUSE, 0);
    }

    @Override
    public Integer playResumeControl(String streamId) {
        PlayListLogEntity playListLogEntity = playControlCommon(streamId);
        Integer playHandle = playListLogEntity.getPlayHandle();
        return iSdkCommderService.playBackControl(playHandle, HCNetSDK.NET_DVR_PLAYRESTART, 0);
    }

    private PlayListLogEntity playControlCommon(String streamId){
        LambdaQueryWrapper<PlayListLogEntity> playListLogEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        playListLogEntityLambdaQueryWrapper.eq(PlayListLogEntity::getPlayStatus,0);
        playListLogEntityLambdaQueryWrapper.eq(PlayListLogEntity::getStreamId,streamId).last("limit 1");
        PlayListLogEntity playListLogEntity = playListLogMapper.selectOne(playListLogEntityLambdaQueryWrapper);
        if(ObjectUtils.isEmpty(playListLogEntity)){
            throw new BusinessException(BusinessErrorEnums.STREAM_NOT_FOUND);
        }
        return playListLogEntity;
    }

    @Override
    public void playSeekControl(String streamId, long seekTime, String msgId) {
        //暂时不考虑这种方式，使用录像回放开始结束的方式
    }

    /**
     * 重启修改全部流列表状态
     */
    @Override
    public void restartStopAll() {
        PlayListLogEntity playListLogEntity = new PlayListLogEntity();
        playListLogEntity.setPlayStatus(2);
        playListLogMapper.update(playListLogEntity,null);
    }
}
