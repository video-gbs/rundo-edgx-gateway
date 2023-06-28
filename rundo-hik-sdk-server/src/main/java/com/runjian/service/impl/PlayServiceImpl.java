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
import com.runjian.domain.dto.commder.PlayInfoDto;
import com.runjian.domain.req.PlaySdkReq;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.entity.DeviceEntity;
import com.runjian.entity.PlayListLogEntity;
import com.runjian.hik.module.service.ISdkCommderService;
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
import org.springframework.beans.factory.annotation.Value;
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


    @Override
    public CommonResponse<Integer> play(PlayReq playReq)  {
        CommonResponse<PlayCommonDto> commonResponse = playCommonCheck(playReq);
        //建立socke连接
        CommonResponse<String> commonResponse1 = streamMediaDeal(playReq);
        if(commonResponse1.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
            throw new BusinessException(BusinessErrorEnums.MEDIA_SERVER_SOCKET_ERROR);
        }
        String socketHandle = commonResponse1.getData();
        PlayCommonDto data = commonResponse.getData();
        int streamMode = playReq.getStreamMode();
        PlayInfoDto play = iSdkCommderService.play(data.getLUserId(), data.getChannelNum(), streamMode, 1);
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

    private synchronized CommonResponse<String> streamMediaDeal(PlayReq playReq) {
        String ip = playReq.getSsrcInfo().getIp();
        int port = playReq.getSsrcInfo().getPort();
        String socketHandle =  UuidUtil.toUuid();
        try {
            Socket socket = new Socket(ip, port);
            ConcurrentHashMap<String, Object> socketHanderMap = playHandleConf.getSocketHanderMap();
            SocketPointer socketPointer = new SocketPointer();
            socketPointer.socketHandle = socketHandle;
            socketHanderMap.put(socketPointer.socketHandle,socket);
        }catch (Exception e){
            return CommonResponse.failure(BusinessErrorEnums.MEDIA_SERVER_SOCKET_ERROR);
        }
        return CommonResponse.success(socketHandle);

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
        playCommonDto.setLUserId(deviceEntity.getLUserId());
        playCommonDto.setChannelNum(deviceChannelEntity.getChannelNum());
        return CommonResponse.success(playCommonDto);

    }
    @Override
    public CommonResponse<Integer> playBack(PlayBackReq playBackReq) {
        CommonResponse<PlayCommonDto> commonResponse = playCommonCheck(playBackReq);
        //建立socke连接
        CommonResponse<String> commonResponse1 = streamMediaDeal(playBackReq);
        if(commonResponse1.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
            throw new BusinessException(BusinessErrorEnums.MEDIA_SERVER_SOCKET_ERROR);
        }
        String socketHandle = commonResponse1.getData();
        PlayCommonDto data = commonResponse.getData();
        int streamMode = playBackReq.getStreamMode();
        PlayInfoDto play = iSdkCommderService.playBack(data.getLUserId(), data.getChannelNum(), playBackReq.getStartTime(), playBackReq.getEndTime());
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
            Socket socket = (Socket)playHandleConf.getSocketHanderMap().get(playListLogEntity.getSocketHandle());

            socket.shutdownOutput();
        }catch (Exception e){
            log.info(LogTemplate.ERROR_LOG_TEMPLATE,"流bye操作","关闭socket失败",e.getMessage());
            throw new BusinessException(BusinessErrorEnums.MEDIA_SERVER_SOCKET_ERROR);
        }

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
