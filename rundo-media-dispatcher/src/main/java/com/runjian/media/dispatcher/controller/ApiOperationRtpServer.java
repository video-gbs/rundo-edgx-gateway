package com.runjian.media.dispatcher.controller;

import com.runjian.common.commonDto.Gb28181Media.req.*;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.validator.ValidatorService;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import com.runjian.media.dispatcher.service.IOnlineStreamsService;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 网关服务通知
 * @author chenjialing
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ApiOperationRtpServer {

    @Autowired
    IMediaPlayService mediaPlayService;



    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private IOnlineStreamsService onlineStreamsService;

    @Autowired
    private ImediaServerService imediaServerService;

    //查看流是否存在
    /**
     *  查看流是否存在
     */
    @PostMapping(value = "/media/streamNotify",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> streamNotifyServer(@RequestBody GatewayBusinessSceneResp gatewayStreamNotify){//获取zlm流媒体配置
        validatorService.validateRequest(gatewayStreamNotify);
        mediaPlayService.streamNotifyServer(gatewayStreamNotify);
        return CommonResponse.success();
    }

    /**
     * rtp推流
     * @param gatewayRtpSendReq
     * @return
     */
    @PostMapping(value = "/media/rtpSendInfo",produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<SsrcInfo> rtpSendInfo(@RequestBody GatewayRtpSendReq gatewayRtpSendReq){

        OnlineStreamsEntity oneBystreamId = onlineStreamsService.streamByChannelInfo(gatewayRtpSendReq.getDeviceId(), gatewayRtpSendReq.getChannelId());
        if(ObjectUtils.isEmpty(oneBystreamId)){
            throw  new BusinessException(BusinessErrorEnums.DB_NOT_FOUND,"推流信息获取失败");

        }else {
            SsrcInfo ssrcInfo = imediaServerService.rtpSendServer(oneBystreamId.getMediaServerId(), oneBystreamId.getApp(), oneBystreamId.getStreamId(), gatewayRtpSendReq);
            //进行流媒体的流转发
            return CommonResponse.success(ssrcInfo);
        }
    }


    /**
     * rtp推流
     * @param webRtcTalkReq
     * @return
     */
    @PostMapping(value = "/media/webRtcTalk",produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<String> webRtcTalk(@RequestBody WebRTCTalkReq webRtcTalkReq){

        return CommonResponse.success(mediaPlayService.webRtcTalk(webRtcTalkReq));

    }



    @PostMapping(value = "/test/playBackDownload")
    public CommonResponse<String> playBackDownload(@RequestBody MediaRecordDownloadReq req){
        mediaPlayService.playRecordDownload(req);
        return CommonResponse.success("成功");

    }

    @PostMapping(value = "/test/playPicDownload")
    public CommonResponse<String> playPicDownload(@RequestBody MediaPictureDownloadReq req){

        mediaPlayService.playPictureDownload(req);
        return CommonResponse.success("成功");

    }

}
