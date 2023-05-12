package com.runjian.media.dispatcher.service.impl;

import com.alibaba.fastjson.JSON;
import com.runjian.common.commonDto.Gateway.dto.SsrcConfig;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.req.*;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.UserSetting;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import com.runjian.media.dispatcher.service.IOnlineStreamsService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import com.runjian.media.dispatcher.zlm.ZLMRESTfulUtils;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.TimeUnit;

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
    ZLMRESTfulUtils zlmresTfulUtils;

    @Autowired
    ImediaServerService mediaServerService;

    @Autowired
    RabbitMqSender rabbitMqSender;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    UserSetting userSetting;

    @Override
    public void play(MediaPlayReq playReq) {
        //不做redisson并发请求控制
        String businessSceneKey = GatewayMsgType.STREAM_LIVE_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+playReq.getStreamId();
        try {
            //阻塞型,默认是30s无返回参数
            SsrcInfo playCommonSsrcInfo = playCommonProcess(businessSceneKey, GatewayMsgType.STREAM_LIVE_PLAY_START, playReq,true);
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "端口创建结果", playCommonSsrcInfo);
            //直播
            if(ObjectUtils.isEmpty(playCommonSsrcInfo)){
                //流复用，不用通知网关
                return;
            }
            PlayReq gatewayPlayReq = new PlayReq();
            gatewayPlayReq.setSsrcInfo(playCommonSsrcInfo);
            gatewayPlayReq.setDeviceId(playReq.getDeviceId());
            gatewayPlayReq.setChannelId(playReq.getChannelId());
            gatewayPlayReq.setStreamMode(playReq.getStreamMode());
            gatewayPlayReq.setDispatchUrl(playReq.getDispatchUrl());
            gatewayPlayReq.setStreamId(playReq.getStreamId());
            //将ssrcinfo通知网关
            CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.PLAY.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,playReq.getMsgId());
            businessMqInfo.setData(gatewayPlayReq);
            rabbitMqSender.sendMsgByExchange(playReq.getGatewayMqExchange(), playReq.getGatewayMqRouteKey(), UuidUtil.toUuid(),businessMqInfo,true);


        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "点播服务", "点播失败", playReq,e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.STREAM_LIVE_PLAY_START,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }
    }

    @Override
    public void playBack(MediaPlayBackReq mediaPlayBackReq) {
        String businessSceneKey = GatewayMsgType.STREAM_RECORD_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+mediaPlayBackReq.getStreamId();
        try {
            //阻塞型,默认是30s无返回参数
            SsrcInfo playCommonSsrcInfo = playCommonProcess(businessSceneKey, GatewayMsgType.STREAM_RECORD_PLAY_START, mediaPlayBackReq,false);
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播回放服务", "端口创建结果", playCommonSsrcInfo);
            if(ObjectUtils.isEmpty(playCommonSsrcInfo)){
                //流复用，不用通知网关
                return;
            }
            //直播
            PlayReq gatewayPlayReq = new PlayReq();
            gatewayPlayReq.setSsrcInfo(playCommonSsrcInfo);
            gatewayPlayReq.setDeviceId(mediaPlayBackReq.getDeviceId());
            gatewayPlayReq.setChannelId(mediaPlayBackReq.getChannelId());
            gatewayPlayReq.setStreamMode(mediaPlayBackReq.getStreamMode());
            gatewayPlayReq.setDispatchUrl(mediaPlayBackReq.getDispatchUrl());
            gatewayPlayReq.setStreamId(mediaPlayBackReq.getStreamId());
            //将ssrcinfo通知网关
            CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.PLAY_BACK.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,mediaPlayBackReq.getMsgId());
            PlayBackReq gatewayPlayBackReq = new PlayBackReq();
            gatewayPlayBackReq.setSsrcInfo(playCommonSsrcInfo);
            gatewayPlayBackReq.setDeviceId(mediaPlayBackReq.getDeviceId());
            gatewayPlayBackReq.setChannelId(mediaPlayBackReq.getChannelId());
            gatewayPlayBackReq.setStreamMode(mediaPlayBackReq.getStreamMode());
            gatewayPlayBackReq.setDispatchUrl(mediaPlayBackReq.getDispatchUrl());
            gatewayPlayBackReq.setStreamId(mediaPlayBackReq.getStreamId());
            gatewayPlayBackReq.setStartTime(mediaPlayBackReq.getStartTime());
            gatewayPlayBackReq.setEndTime(mediaPlayBackReq.getEndTime());


            businessMqInfo.setData(gatewayPlayBackReq);

            rabbitMqSender.sendMsgByExchange(mediaPlayBackReq.getGatewayMqExchange(), mediaPlayBackReq.getGatewayMqRouteKey(), UuidUtil.toUuid(),businessMqInfo,true);


        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "点播回放服务", "回放失败", mediaPlayBackReq,e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.STREAM_LIVE_PLAY_START,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }

    }

    @Override
    public StreamInfo playCustom(CustomPlayReq customPlayReq) {
        //复用流判断
        String streamId = customPlayReq.getStreamId();
        MediaServerItem oneMedia;

        OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(streamId);
        if(!ObjectUtils.isEmpty(oneBystreamId)){
            //判断流复用
            oneMedia = mediaServerService.getOne(oneBystreamId.getMediaServerId());
            if(!mediaServerService.checkRtpServer(oneMedia, streamId)){
                //流其实不存在
                onlineStreamsService.remove(streamId);

            }else{
                //留存在 直接返回
                StreamInfo streamInfoByAppAndStream = mediaServerService.getStreamInfoByAppAndStream(oneMedia, oneBystreamId.getApp(), streamId);

                return streamInfoByAppAndStream;
            }

        }else {
            //获取默认的zlm流媒体
            oneMedia =  mediaServerService.getDefaultMediaServer();
        }

        StreamInfo streamInfoByAppAndStream = mediaServerService.getStreamInfoByAppAndStream(oneMedia, VideoManagerConstants.PUSH_LIVE_APP, streamId);
        customPlayReq.setMediaServerId(oneMedia.getId());
        RedisCommonUtil.set(redisTemplate,VideoManagerConstants.MEDIA_PUSH_STREAM_REQ+ BusinessSceneConstants.SCENE_SEM_KEY+streamId,customPlayReq);

        return streamInfoByAppAndStream;

    }

    private SsrcInfo playCommonProcess(String businessSceneKey, GatewayMsgType gatewayMsgType, MediaPlayReq playReq, boolean isPlay) throws InterruptedException {

        redisCatchStorageService.addBusinessSceneKey(businessSceneKey,gatewayMsgType,playReq.getMsgId());
        RLock lock = redissonClient.getLock(businessSceneKey);
        //尝试获取锁
        boolean b = lock.tryLock(0,userSetting.getBusinessSceneTimeout()+100, TimeUnit.MILLISECONDS);
        if(!b){
            //加锁失败，不继续执行
            log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"点播请求，合并全局的请求",businessSceneKey);
            return null;
        }
        String streamId = playReq.getStreamId();
        MediaServerItem oneMedia;
        // 复用流判断 针对直播场景
        if(isPlay){
            //复用流判断
            OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(playReq.getStreamId());
            if(!ObjectUtils.isEmpty(oneBystreamId)){
                //判断流复用
                oneMedia = mediaServerService.getOne(oneBystreamId.getMediaServerId());
                if(!mediaServerService.checkRtpServer(oneMedia, streamId)){
                    //流其实不存在
                    onlineStreamsService.remove(streamId);
                    
                }else{
                    //留存在 直接返回
                    StreamInfo streamInfoByAppAndStream = mediaServerService.getStreamInfoByAppAndStream(oneMedia, oneBystreamId.getApp(), streamId);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.SUCCESS,streamInfoByAppAndStream);
                    return null;
                }


            }

        }
        //获取默认的zlm流媒体
        oneMedia =  mediaServerService.getDefaultMediaServer();
        GatewayBindReq gatewayBindReq = new GatewayBindReq();
        gatewayBindReq.setMqExchange(playReq.getGatewayMqExchange());
        gatewayBindReq.setMqRouteKey(playReq.getGatewayMqRouteKey());
        gatewayBindReq.setMqQueueName(playReq.getGatewayMqRouteKey());
        gatewayBindReq.setDispatchUrl(playReq.getDispatchUrl());
        //收流端口创建
        BaseRtpServerDto baseRtpServerDto = new BaseRtpServerDto();
        baseRtpServerDto.setDeviceId(playReq.getDeviceId());
        baseRtpServerDto.setChannelId(playReq.getChannelId());
        baseRtpServerDto.setEnableAudio(playReq.getEnableAudio());
        baseRtpServerDto.setStreamId(playReq.getStreamId());
        baseRtpServerDto.setGatewayBindReq(gatewayBindReq);
        baseRtpServerDto.setRecordState(playReq.getRecordState());
        baseRtpServerDto.setStreamMode(playReq.getStreamMode());
        baseRtpServerDto.setMediaServerId(oneMedia.getId());
        if(playReq.getSsrcCheck()){
            SsrcConfig ssrcConfig = redisCatchStorageService.getSsrcConfig();
            if(isPlay){
                baseRtpServerDto.setSsrc(ssrcConfig.getPlaySsrc());
            }else {
                baseRtpServerDto.setSsrc(ssrcConfig.getPlayBackSsrc());
            }
            //更新ssrc的缓存
            redisCatchStorageService.setSsrcConfig(ssrcConfig);
        }
        //缓存相关的请求参数  用于on_publish的回调 以及停止点播
        RedisCommonUtil.set(redisTemplate,VideoManagerConstants.MEDIA_RTP_SERVER_REQ+ BusinessSceneConstants.SCENE_SEM_KEY+baseRtpServerDto.getStreamId(),baseRtpServerDto);
        return mediaServerService.openRTPServer(oneMedia, baseRtpServerDto,gatewayMsgType,businessSceneKey);

    }

    @Override
    public void streamNotifyServer(GatewayStreamNotify gatewayStreamNotify) {
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"点播通知", JSON.toJSONString(gatewayStreamNotify));
        String streamId = gatewayStreamNotify.getStreamId();
        BusinessSceneResp businessSceneResp = gatewayStreamNotify.getBusinessSceneResp();
        //判断点播回放
        GatewayMsgType gatewayMsgType = businessSceneResp.getGatewayMsgType();
        String businessKey;
        GatewayMsgType gatewayType = GatewayMsgType.STREAM_LIVE_PLAY_START;
        if(gatewayMsgType.equals(GatewayMsgType.PLAY)){
            //直播
            businessKey = GatewayMsgType.STREAM_LIVE_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamId;
        }else {
            businessKey = GatewayMsgType.STREAM_RECORD_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamId;
            gatewayType = GatewayMsgType.STREAM_RECORD_PLAY_START;
        }
        BusinessErrorEnums oneBusinessNum = BusinessErrorEnums.getOneBusinessNum(businessSceneResp.getCode());
        //设备交互成功，状态为进行状态
        redisCatchStorageService.editRunningBusinessSceneKey(businessKey,gatewayType,oneBusinessNum,null);
    }
    @Async("taskExecutor")
    @Override
    public void playBusinessErrorScene(String businessKey, BusinessSceneResp businessSceneResp) {
        //点播相关的key的组合条件
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播失败，异常处理流程", businessSceneResp);
        //处理sip交互成功，但是流注册未返回的情况
        String streamId = businessKey.substring(businessKey.indexOf(BusinessSceneConstants.SCENE_SEM_KEY)+1);
        //通知网关bye
        mediaServerService.streamBye(streamId,null);

    }
}
