package com.runjian.media.dispatcher.zlm.controller;

import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.CloseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.RtpInfoDto;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.validator.ValidatorService;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.service.IGatewayBindService;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 操作zlm开启与关闭rtp端口操作
 * @author chenjialing
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ApiOperationRtpServer {

    @Autowired
    ImediaServerService imediaServerService;

    @Autowired
    IGatewayBindService gatewayBindService;

    @Autowired
    private ValidatorService validatorService;

    /**
     *
     */
    @PostMapping(value = "/media/gatewayBind",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse gaetwayBind(@RequestBody GatewayBindReq gatewayBindReq){
        validatorService.validateRequest(gatewayBindReq);
        gatewayBindService.edit(gatewayBindReq);

        return CommonResponse.success();
    }

    /**
     * 创建推流的端口
     */
    @PostMapping(value = "/media/openRtpServer",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<SsrcInfo> openRtpServer(@RequestBody BaseRtpServerDto baseRtpServerDto){
        validatorService.validateRequest(baseRtpServerDto);
        //获取zlm流媒体配置
        MediaServerItem defaultMediaServer = imediaServerService.getDefaultMediaServer();
        if(baseRtpServerDto.getMediaserverId()){
            MediaServerItem defaultMediaServer = imediaServerService.getOne(baseRtpServerDto.getMediaserverId());
        }else {
            MediaServerItem defaultMediaServer = imediaServerService.getDefaultMediaServer();
        }
        baseRtpServerDto.setMeidaServerId(defaultMediaServer.getId());
        SsrcInfo ssrcInfo = imediaServerService.openRTPServer(defaultMediaServer, baseRtpServerDto);

        return CommonResponse.success(ssrcInfo);
    }

    /**
     * 关闭推流的端口
     */
    @PostMapping(value = "/media/closeRtpServer",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> closeRtpServer(@RequestBody CloseRtpServerDto closeRtpServerDto){
        validatorService.validateRequest(closeRtpServerDto);
        //获取zlm流媒体配置

        return CommonResponse.success(imediaServerService.closeRTPServer(closeRtpServerDto.getMediaServerId(), closeRtpServerDto.getStreamId()));
    }

    //查看流是否存在
    /**
     *  查看流是否存在
     */
    @PostMapping(value = "/media/getRtpInfo",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<StreamInfo> getRtpServer(@RequestBody RtpInfoDto rtpInfoDto){
        validatorService.validateRequest(rtpInfoDto);
        //获取zlm流媒体配置
        StreamInfo rtpInfo = imediaServerService.getRtpInfo(rtpInfoDto.getMediaServerId(), rtpInfoDto.getStreamId(), rtpInfoDto.getApp());

        return CommonResponse.success(rtpInfo);

    }
    //查看流是否存在
    /**
     *  查看流是否存在
     */
    @PostMapping(value = "/media/streamNotify",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> streamNotifyServer(@RequestBody String streamId){
        //获取zlm流媒体配置
        return CommonResponse.success(imediaServerService.streamNotify(streamId));

    }
}
