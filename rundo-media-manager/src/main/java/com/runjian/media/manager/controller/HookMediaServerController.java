package com.runjian.media.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 注册
     * @param req
     * @return
     */
    @PostMapping(value = "/registerMediaNode",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> registerMediaNode(@RequestBody MediaServerConfigDto req){//获取zlm流媒体配置
        log.info("请求={}",req);
        return CommonResponse.success();
    }

    /**
     * 心跳
     * @param req
     * @return
     */
    @PostMapping(value = "/updateKeepalive",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> updateKeepalive(@RequestBody JSONObject req){//获取zlm流媒体配置
        log.info("请求={}",req);
        return CommonResponse.success();
    }

    /**
     * 注销
     * @param req
     * @return
     */
    @PostMapping(value = "/unregisterMediaNode",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> unregisterMediaNode(@RequestBody MediaServerConfigDto req){
        log.info("请求={}",req);
        return CommonResponse.success();
    }
}
