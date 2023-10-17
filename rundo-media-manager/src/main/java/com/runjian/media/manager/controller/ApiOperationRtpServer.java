package com.runjian.media.manager.controller;

import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.validator.ValidatorService;
import com.runjian.media.manager.service.IMediaPlayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
