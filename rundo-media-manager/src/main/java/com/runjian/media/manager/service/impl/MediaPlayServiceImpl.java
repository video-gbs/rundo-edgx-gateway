package com.runjian.media.manager.service.impl;

import com.alibaba.fastjson.JSON;
import com.runjian.common.commonDto.Gateway.dto.SsrcConfig;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.manager.service.IMediaPlayService;
import com.runjian.media.manager.service.IOnlineStreamsService;
import com.runjian.media.manager.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class MediaPlayServiceImpl implements IMediaPlayService {
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    IOnlineStreamsService onlineStreamsService;


    @Autowired
    RabbitMqSender rabbitMqSender;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void play(MediaPlayReq playReq) {
        //不做redisson并发请求控制

    }

    @Override
    public void playBack(MediaPlayBackReq mediaPlayBackReq) {


    }

    private SsrcInfo playCommonProcess(String businessSceneKey, GatewayMsgType gatewayMsgType, MediaPlayReq playReq, boolean isPlay) throws InterruptedException {
        return null;
    }

    @Override
    public void streamNotifyServer(GatewayStreamNotify gatewayStreamNotify) {

    }

    @Async("taskExecutor")
    @Override
    public void playBusinessErrorScene(StreamBusinessSceneResp businessSceneResp ) {
        //点播相关的key的组合条件
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播失败，异常处理流程", businessSceneResp);
        String businessSceneKey = businessSceneResp.getBusinessSceneKey();


    }
}
