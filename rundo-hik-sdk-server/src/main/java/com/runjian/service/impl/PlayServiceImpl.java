package com.runjian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.conf.PlayHandleConf;
import com.runjian.domain.dto.PlayCommonDto;
import com.runjian.domain.dto.commder.PlayInfoDto;
import com.runjian.domain.req.PlaySdkReq;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.entity.DeviceEntity;
import com.runjian.entity.PlayListLogEntity;
import com.runjian.hik.module.service.ISdkCommderService;
import com.runjian.mapper.PlayListLogMapper;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
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
    public CommonResponse<Integer> play(PlayReq playReq) {
        CommonResponse<PlayCommonDto> commonResponse = playCommonCheck(playReq);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            return CommonResponse.failure(BusinessErrorEnums.getOneBusinessNum(commonResponse.getCode()));
        }

        PlayCommonDto data = commonResponse.getData();
        int streamMode = "TCP".equals(playReq.getStreamMode())?0:1;
        PlayInfoDto play = iSdkCommderService.play(data.getLUserId(), data.getChannelNum(), 1, streamMode);
        int errorCode = play.getErrorCode();
        if(errorCode == 0){
            CommonResponse commonResponse1 = streamMediaDeal(playReq, play);
            if(commonResponse1.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
                //关闭sdk的流
                streamBye(playReq.getStreamId());
                return CommonResponse.failure(BusinessErrorEnums.getOneBusinessNum(commonResponse1.getCode()));
            }
        }
        int playStatus = errorCode==0?0:-1;
        PlayListLogEntity playListLogEntity = new PlayListLogEntity();
        playListLogEntity.setStreamId(playReq.getStreamId());
        playListLogEntity.setPlayErrorCode(errorCode);
        playListLogEntity.setPlayHandle(play.getLPreviewHandle());
        playListLogEntity.setPlayStatus(playStatus);
        playListLogMapper.insert(playListLogEntity);
        return errorCode==0?CommonResponse.success(errorCode):CommonResponse.failure(BusinessErrorEnums.SDK_OPERATION_FAILURE,BusinessErrorEnums.SDK_OPERATION_FAILURE.getErrMsg()+errorCode);

    }

    private CommonResponse streamMediaDeal(PlayReq playReq,PlayInfoDto play){
        String ip = playReq.getSsrcInfo().getIp();
        int port = playReq.getSsrcInfo().getPort();
        int streamMode = 1;
        String streamId = playReq.getStreamId();

        String url = openSdkServerApi.replace("{ip}", ip).replace("{port}", String.valueOf(port)).replace("{tcpMode}", String.valueOf(streamMode)).replace("{streamId}", streamId);

        String result = RestTemplateUtil.get(url, null, restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"自研流媒体服务连接","连接业务异常",result);
            return CommonResponse.failure(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",commonResponse);
            return CommonResponse.failure(BusinessErrorEnums.MEDIA_SERVER_COLLECT_ERROR);
        }
        Integer mediaPort = (Integer)commonResponse.getData();
        try{
            Socket socket = new Socket(serverIp, mediaPort);
            ConcurrentHashMap<Integer, Object> socketHanderMap = playHandleConf.getSocketHanderMap();
            socketHanderMap.put(play.getLPreviewHandle(),socket);
        }catch (Exception e){
            String closeUrl = closeSdkServerApi.replace("{streamId}", streamId);
            String closeResult = RestTemplateUtil.get(closeUrl, null, restTemplate);
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"自研流媒体服务连接-socket-连接业务异常",closeResult,e);
            return CommonResponse.failure(BusinessErrorEnums.MEDIA_SERVER_SOCKET_ERROR);
        }

        return CommonResponse.success();
    }


    private CommonResponse<PlayCommonDto> playCommonCheck(PlayReq playReq){
        //获取设备信息luserId
        long encodeId = Long.parseLong(playReq.getDeviceId());
        DeviceEntity deviceEntity = deviceService.getById(encodeId);
        if(ObjectUtils.isEmpty(deviceEntity)){

            return CommonResponse.failure(BusinessErrorEnums.DB_DEVICE_NOT_FOUND);
        }else {
            if(deviceEntity.getOnline() != 1){
                return CommonResponse.failure(BusinessErrorEnums.DB_DEVICE_NOT_FOUND);
            }
        }
        //获取通道信息

        long channelId = Long.parseLong(playReq.getChannelId());
        DeviceChannelEntity deviceChannelEntity = deviceChannelService.getById(channelId);
        if(ObjectUtils.isEmpty(deviceChannelEntity)){
            return CommonResponse.failure(BusinessErrorEnums.DB_CHANNEL_NOT_FOUND);
        }else {
            if(deviceChannelEntity.getOnline() != 1){
                return CommonResponse.failure(BusinessErrorEnums.CHANNEL_OFFLINE);

            }
        }

        PlayCommonDto playCommonDto = new PlayCommonDto();
        playCommonDto.setLUserId(deviceEntity.getLUserId());
        playCommonDto.setChannelNum(deviceChannelEntity.getChannelNum());
        return CommonResponse.success(playCommonDto);


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
    public Boolean streamBye(String streamId) {
        log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流bye操作","bye进入",streamId);
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
        //关闭自研流媒体
        String url = closeSdkServerApi.replace("{streamId}", streamId);
        String result = RestTemplateUtil.get(url, null, restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"自研流媒体服务连接","连接业务异常",result);
            return false;
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
