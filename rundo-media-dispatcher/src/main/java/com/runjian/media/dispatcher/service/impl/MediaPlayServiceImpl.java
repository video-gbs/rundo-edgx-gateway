package com.runjian.media.dispatcher.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.SsrcConfig;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.req.*;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamCheckListResp;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamCloseDto;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.UserSetting;
import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.service.IGatewayDealMsgService;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import com.runjian.media.dispatcher.service.IOnlineStreamsService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import com.runjian.media.dispatcher.zlm.ZLMRESTfulUtils;
import com.runjian.media.dispatcher.zlm.ZlmHttpHookSubscribe;
import com.runjian.media.dispatcher.zlm.dto.HookSubscribeFactory;
import com.runjian.media.dispatcher.zlm.dto.HookSubscribeForStreamChange;
import com.runjian.media.dispatcher.zlm.dto.HookType;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private ZlmHttpHookSubscribe subscribe;

    @Autowired
    IGatewayDealMsgService gatewayDealMsgService;

    @Autowired
    DispatcherSignInConf dispatcherSignInConf;
    @Override
    public void play(MediaPlayReq playReq) {
        //不做redisson并发请求控制
        String businessSceneKey = StreamBusinessMsgType.STREAM_LIVE_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+playReq.getStreamId();
        try {
            //阻塞型,默认是30s无返回参数
            SsrcInfo playCommonSsrcInfo = playCommonProcess(businessSceneKey, StreamBusinessMsgType.STREAM_LIVE_PLAY_START, playReq,true);
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "端口创建结果", playCommonSsrcInfo);
            //直播
            if(ObjectUtils.isEmpty(playCommonSsrcInfo)){
                //流复用，不用通知网关
                return;
            }
            gatewayDealMsgService.sendGatewayPlayMsg(playCommonSsrcInfo,playReq);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "点播服务", "点播失败", playReq,e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,StreamBusinessMsgType.STREAM_LIVE_PLAY_START,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }
    }

    @Override
    public void playBack(MediaPlayBackReq mediaPlayBackReq) {
        String businessSceneKey = StreamBusinessMsgType.STREAM_RECORD_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+mediaPlayBackReq.getStreamId();
        try {
            //阻塞型,默认是30s无返回参数
            SsrcInfo playCommonSsrcInfo = playCommonProcess(businessSceneKey, StreamBusinessMsgType.STREAM_RECORD_PLAY_START, mediaPlayBackReq,false);
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播回放服务", "端口创建结果", playCommonSsrcInfo);
            if(ObjectUtils.isEmpty(playCommonSsrcInfo)){
                //流复用，不用通知网关
                return;
            }

            gatewayDealMsgService.sendGatewayPlayBackMsg(playCommonSsrcInfo,mediaPlayBackReq);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "点播回放服务", "回放失败", mediaPlayBackReq,e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,StreamBusinessMsgType.STREAM_RECORD_PLAY_START,BusinessErrorEnums.UNKNOWN_ERROR,null);
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
        //流信息状态保存
        OnlineStreamsEntity onlineStreamsEntity = new OnlineStreamsEntity();
        BeanUtil.copyProperties(customPlayReq,onlineStreamsEntity);
        onlineStreamsEntity.setMediaServerId(oneMedia.getId());
        onlineStreamsEntity.setMediaServerId(oneMedia.getId());
        onlineStreamsEntity.setStreamType(1);
        onlineStreamsService.save(onlineStreamsEntity);
        return streamInfoByAppAndStream;

    }

    private SsrcInfo playCommonProcess(String businessSceneKey, StreamBusinessMsgType msgType, MediaPlayReq playReq, boolean isPlay) throws InterruptedException {
        Boolean aBoolean = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, msgType, playReq.getMsgId());
        //尝试获取锁
        if(!aBoolean){
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
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,msgType,BusinessErrorEnums.SUCCESS,streamInfoByAppAndStream);
                    return null;
                }


            }

        }
        //获取默认的zlm流媒体
        oneMedia =  mediaServerService.getDefaultMediaServer();

        String ssrc = "";
        if(playReq.getSsrcCheck()){
            SsrcConfig ssrcConfig = redisCatchStorageService.getSsrcConfig();
            if(isPlay){
                ssrc = ssrcConfig.getPlaySsrc();
            }else {
                ssrc = ssrcConfig.getPlayBackSsrc();
            }
            //更新ssrc的缓存
            redisCatchStorageService.setSsrcConfig(ssrcConfig);
        }
        //流注册成功 回调
        HookSubscribeForStreamChange hookSubscribe = HookSubscribeFactory.on_stream_changed(VideoManagerConstants.GB28181_APP, playReq.getStreamId(),true, VideoManagerConstants.GB28181_SCHEAM ,oneMedia.getId());
        MediaServerItem finalOneMedia = oneMedia;
        subscribe.addSubscribe(hookSubscribe, (MediaServerItem mediaServerItemInUse, JSONObject json) -> {
            //流注册处理  发送指定mq消息
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm推流注册成功通知", "收到推流订阅消息", json.toJSONString());
            //拼接拉流的地址
            String pushStreamId = json.getString("stream");
            StreamInfo streamInfoByAppAndStream = mediaServerService.getStreamInfoByAppAndStream(finalOneMedia, VideoManagerConstants.GB28181_APP, pushStreamId);
            //发送调度服务的业务队列 通知流实际成功
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,msgType,BusinessErrorEnums.SUCCESS,streamInfoByAppAndStream);
            //流状态修改为成功
            // hook响应
            subscribe.removeSubscribe(hookSubscribe);
        });
        SsrcInfo ssrcInfo = mediaServerService.openRTPServer(oneMedia, playReq.getStreamId(), ssrc, playReq.getSsrcCheck(), 0);
        //流信息状态保存
        OnlineStreamsEntity onlineStreamsEntity = new OnlineStreamsEntity();
        BeanUtil.copyProperties(playReq,onlineStreamsEntity);
        onlineStreamsEntity.setMediaServerId(oneMedia.getId());
        onlineStreamsService.save(onlineStreamsEntity);
        return ssrcInfo;

    }

    @Override
    public void streamNotifyServer(GatewayStreamNotify gatewayStreamNotify) {
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"点播通知", JSON.toJSONString(gatewayStreamNotify));
        String streamId = gatewayStreamNotify.getStreamId();
        GatewayBusinessSceneResp businessSceneResp = gatewayStreamNotify.getBusinessSceneResp();
        //判断点播回放
        GatewayBusinessMsgType businessMsgType = businessSceneResp.getGatewayMsgType();
        String businessKey;
        StreamBusinessMsgType gatewayType = StreamBusinessMsgType.STREAM_LIVE_PLAY_START;
        if(businessMsgType.equals(GatewayBusinessMsgType.PLAY)){
            //直播
            businessKey = StreamBusinessMsgType.STREAM_LIVE_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamId;
        }else {
            businessKey = StreamBusinessMsgType.STREAM_RECORD_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamId;
            gatewayType = StreamBusinessMsgType.STREAM_RECORD_PLAY_START;
        }
        BusinessErrorEnums oneBusinessNum = BusinessErrorEnums.getOneBusinessNum(businessSceneResp.getCode());
        if(!oneBusinessNum.equals(BusinessErrorEnums.COMMDER_SEND_SUCESS)){
            redisCatchStorageService.editBusinessSceneKey(businessKey,gatewayType,oneBusinessNum,null);
        }

    }
    @Async("taskExecutor")
    @Override
    public void playBusinessErrorScene(StreamBusinessSceneResp businessSceneResp ) {
        //点播相关的key的组合条件
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播失败，异常处理流程", businessSceneResp);
        //处理sip交互成功，但是流注册未返回的情况
        String businessKey = businessSceneResp.getBusinessSceneKey();
        String streamId = businessKey.substring(businessKey.indexOf(BusinessSceneConstants.SCENE_SEM_KEY)+1);
        //通知网关bye
        streamBye(streamId,businessSceneResp.getMsgId());

    }

    /**
     * 通知网关停止流
     * @param streamId
     * @param msgId
     */
    @Override
    public synchronized void streamBye(String streamId,String msgId){
        //主动管理流的关闭
        OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(streamId);
        if(ObjectUtils.isEmpty(oneBystreamId)){
            if(oneBystreamId.getStreamType() == 0){
                //订阅流主销处理
                //流注销成功 回调
                HookSubscribeForStreamChange hookSubscribe = HookSubscribeFactory.on_stream_changed(VideoManagerConstants.GB28181_APP, oneBystreamId.getStreamId(),false, VideoManagerConstants.GB28181_SCHEAM ,oneBystreamId.getMediaServerId());
                subscribe.addSubscribe(hookSubscribe, (MediaServerItem mediaServerItemInUse, JSONObject json) -> {
                    //流注册处理  发送指定mq消息
                    log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm推流注销通知", "收到推流注销消息", json.toJSONString());
                    mediaServerService.closeRTPServer(oneBystreamId.getMediaServerId(),streamId);
                    //释放ssrc
                    redisCatchStorageService.ssrcRelease(oneBystreamId.getSsrc());
                    //清除点播请求
                    onlineStreamsService.remove(streamId);
                    // hook响应
                    subscribe.removeSubscribe(hookSubscribe);
                });
                //网关流注销通知
                gatewayDealMsgService.sendGatewayStreamBye(streamId,msgId,oneBystreamId);
            }
        }

    }

    @Async("taskExecutor")
    @Override
    public void streamChangeDeal(JSONObject json) {
        Boolean regist = json.getBoolean("regist");
        String streamId = json.getString("stream");
        String schema = json.getString("schema");
        String app = json.getString("app");
        String mediaServerId = json.getString("mediaServerId");
        //仅仅对于rtsp进行处理  防止处理重复
        if(VideoManagerConstants.GB28181_SCHEAM.equals(schema)){
            OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(streamId);
            if(ObjectUtils.isEmpty(oneBystreamId)){
                //异常推流，暂不处理
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "zlm推流注册异常", "非正常请求点播流", streamId);
            }else {

                if(!regist){
                    //注销
                    if(oneBystreamId.getStreamType() == 0){
                        //没有关闭请求 一定为异常断流
                        //返回订阅的信息
                        ZlmHttpHookSubscribe.Event subscribe = this.subscribe.sendNotify(HookType.on_stream_changed, json);
                        if (subscribe != null ) {
                            //返回订阅的信息
                            MediaServerItem mediaInfo = mediaServerService.getOne(mediaServerId);
                            subscribe.response(mediaInfo, json);
                        }else {
                            //国标异常断流处理
                            streamBye(streamId,null);
                            //进行上层消息发送
                            streamCloseSend(streamId,false);
                        }

                    }else {
                        streamCloseSend(streamId,false);
                        onlineStreamsService.remove(streamId);
                    }


                }else {
                    //注册成功  自行进行业务处理
                    ZlmHttpHookSubscribe.Event subscribe = this.subscribe.sendNotify(HookType.on_stream_changed, json);
                    if (subscribe != null ) {
                        //返回订阅的信息
                        MediaServerItem mediaInfo = mediaServerService.getOne(mediaServerId);
                        subscribe.response(mediaInfo, json);
                    }
                    //修改流的数据库状态
                    oneBystreamId.setStatus(1);
                    onlineStreamsService.update(oneBystreamId);
                }

            }
        }

    }
    @Override
    public List<OnlineStreamsEntity> streamListByStreamIds(StreamCheckListResp streamCheckListResp, String msgId) {
        List<String> streamLists = streamCheckListResp.getStreamIdList();
        if(CollectionUtils.isEmpty(streamLists)){
            log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "流检查为空", streamCheckListResp);
            return null;
        }
        LocalDateTime checkTime = streamCheckListResp.getCheckTime();
        //获取数据库中的数据
        List<OnlineStreamsEntity> onlineStreamsEntities = onlineStreamsService.streamListByCheckTime(streamLists,checkTime);
        List<String> collect = onlineStreamsEntities.stream().map(OnlineStreamsEntity::getStreamId).collect(Collectors.toList());
        //获取差集
        List<String> streamListsBye = new ArrayList<>();
        collect.forEach(streamId->{
            if(!streamLists.contains(streamId)){
                //脏数据或则遗留数据，进行流的bye 关闭
                streamBye(streamId,null);
            }
        });
        //业务队列发送流的列表
        CommonMqDto mqinfo = redisCatchStorageService.getMqInfo(StreamBusinessMsgType.STREAM_CHECK_STREAM.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
        mqinfo.setData(collect);
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "流检查发送", mqinfo);
        rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), dispatcherSignInConf.getMqSetQueue(), UuidUtil.toUuid(),mqinfo,true);

        return onlineStreamsEntities;
    }

    @Override
    public void streamStopAll() {
        //删除全部的当前的流列表数据
        List<OnlineStreamsEntity> onlineStreamsEntities = onlineStreamsService.streamAll();

        if(!CollectionUtils.isEmpty(onlineStreamsEntities)){
            onlineStreamsEntities.forEach(onlineStreamsEntity -> {
                streamBye(onlineStreamsEntity.getStreamId(),null);
            });
        }
    }

    @Override
    public Boolean streamCloseSend(String streamId,Boolean canClose) {
        //国标异常断流处理
        //进行网关消息发送
        StreamCloseDto streamCloseDto = new StreamCloseDto();
        streamCloseDto.setStreamId(streamId);
        streamCloseDto.setCanClose(canClose);
        CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(StreamBusinessMsgType.STREAM_CLOSE.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,null);

        mqInfo.setData(streamCloseDto);
        mqInfo.setData(streamCloseDto);
        rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), dispatcherSignInConf.getMqSetQueue(), UuidUtil.toUuid(),mqInfo,true);
        return true;
    }

    @Override
    public Boolean onStreamNoneReader(String app,String streamId) {

        if (VideoManagerConstants.GB28181_APP.equals(app)){
            // 国标流， 点播/录像回放/录像下载
            CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(StreamBusinessMsgType.STREAM_CLOSE.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,null);
            StreamCloseDto streamCloseDto = new StreamCloseDto();
            streamCloseDto.setStreamId(streamId);
            streamCloseDto.setCanClose(true);
            mqInfo.setData(streamCloseDto);
            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), dispatcherSignInConf.getMqSetQueue(), UuidUtil.toUuid(),mqInfo,true);
            return false;

        }else {
            return true;
        }
    }
}
