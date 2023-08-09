package com.runjian.media.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.dto.hook.KeepaliveServerDto;
import com.runjian.media.manager.dto.dto.hook.StreamChangeDto;
import com.runjian.media.manager.service.IMediaPlayService;
import com.runjian.media.manager.service.IMediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenjialing
 */
@RestController
@RequestMapping("/index/hook")
@Slf4j
public class HookMediaServerController {
    @Autowired
    IMediaServerService mediaServerService;

    @Autowired
    IMediaPlayService mediaPlayService;
    /**
     * 注册
     * @param req
     * @return
     */
    @PostMapping(value = "/onRegisterMediaNode",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> registerMediaNode(@RequestBody MediaServerConfigDto req){//获取zlm流媒体配置
        mediaServerService.registerMediaNode(req);
        return CommonResponse.success();
    }

    /**
     * 心跳
     * @param req
     * @return
     */
    @PostMapping(value = "/onServerKeepalive",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> updateKeepalive(@RequestBody KeepaliveServerDto req){//获取zlm流媒体配置
        mediaServerService.updateMediaServerKeepalive(req.getMediaServerId());
        return CommonResponse.success();
    }

    /**
     * 注销
     * @param req
     * @return
     */
    @PostMapping(value = "/onUnregisterMediaNode",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> unregisterMediaNode(@RequestBody KeepaliveServerDto req){
        mediaServerService.updateMediaServerKeepalive(req.getMediaServerId());
        return CommonResponse.success();
    }

    @PostMapping(value = "/onStreamArrive",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> onStreamArrive(@RequestBody StreamChangeDto req){
        log.info("流通知--到达，请求={}",req);
        mediaPlayService.streamChangeDeal(req,true);
        return CommonResponse.success();
    }

    @PostMapping(value = "/onStreamNoneArrive",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> onStreamNoneArrive(@RequestBody StreamChangeDto req){
        log.info("流通知--未到达，请求={}",req);
//        mediaPlayService.streamChangeDeal(req,true);
        return CommonResponse.success();
    }


    @PostMapping(value = "/onStreamDisconnect",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> onStreamDisconnect(@RequestBody StreamChangeDto req){
        log.info("流通知--断开，请求={}",req);
        mediaPlayService.streamChangeDeal(req,false);
        return CommonResponse.success();
    }

    @PostMapping(value = "/onStreamNoneReader",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> onStreamNoneReader(@RequestBody StreamChangeDto req){
        log.info("流通知--无人观看，请求={}",req);
        mediaPlayService.onStreamNoneReader(req.getApp(),req.getStreamId());
        return CommonResponse.success();
    }

    @PostMapping(value = "/onStreamNotFound",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> onStreamNotFound(@RequestBody StreamChangeDto req){
        log.info("流通知--流地址不存在，请求={}",req);
        mediaPlayService.onStreamNoneReader(req.getApp(),req.getStreamId());
        return CommonResponse.success();
    }
}
