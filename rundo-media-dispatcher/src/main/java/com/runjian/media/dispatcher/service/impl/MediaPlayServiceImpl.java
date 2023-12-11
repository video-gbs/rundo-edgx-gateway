package com.runjian.media.dispatcher.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.SsrcConfig;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.GatewayBusinessNotifyReq;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.ZlmStreamDto;
import com.runjian.common.commonDto.Gb28181Media.req.*;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamCheckListResp;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamCloseDto;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.DynamicTask;
import com.runjian.media.dispatcher.conf.UserSetting;
import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.service.IGatewayDealMsgService;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import com.runjian.media.dispatcher.service.IOnlineStreamsService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import com.runjian.media.dispatcher.zlm.ZLMRESTfulUtils;
import com.runjian.media.dispatcher.zlm.ZlmHttpHookSubscribe;
import com.runjian.media.dispatcher.zlm.dto.*;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import com.runjian.media.dispatcher.zlm.service.impl.MediaServerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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


    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private DynamicTask dynamicTask;
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
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,StreamBusinessMsgType.STREAM_LIVE_PLAY_START,BusinessErrorEnums.UNKNOWN_ERROR,e.getMessage());
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
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,StreamBusinessMsgType.STREAM_RECORD_PLAY_START,BusinessErrorEnums.UNKNOWN_ERROR,e.getMessage());
        }

    }

    @Override
    public StreamInfo playCustom(CustomPlayReq customPlayReq){
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
        onlineStreamsEntity.setStreamType(1);
        onlineStreamsEntity.setMediaServerId(oneMedia.getId());
        onlineStreamsEntity.setApp(VideoManagerConstants.PUSH_LIVE_APP);
        onlineStreamsService.save(onlineStreamsEntity);
        return streamInfoByAppAndStream;

    }

    @Override
    public void playRecordDownload(MediaRecordDownloadReq req) {
        //进行回放流的获取和录制
        String businessSceneKey = StreamBusinessMsgType.STREAM_RECORD_DOWNLOAD.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+req.getStreamId();
        try {
            //阻塞型,默认是30s无返回参数
            SsrcInfo playCommonSsrcInfo = playDownloadProcess(businessSceneKey, StreamBusinessMsgType.STREAM_RECORD_DOWNLOAD,req);
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播回放服务", "端口创建结果", playCommonSsrcInfo);
            if(ObjectUtils.isEmpty(playCommonSsrcInfo)){
                //流复用，不用通知网关
                return;
            }
            MediaPlayBackReq mediaPlayBackReq = new MediaPlayBackReq();
            BeanUtil.copyProperties(req,mediaPlayBackReq);
            gatewayDealMsgService.sendGatewayPlayBackMsg(playCommonSsrcInfo,mediaPlayBackReq);

            //流注册成功 回调
            HookSubscribeForStreamChange hookSubscribe = HookSubscribeFactory.on_stream_changed(VideoManagerConstants.GB28181_APP, req.getStreamId(),true, VideoManagerConstants.GB28181_SCHEAM ,playCommonSsrcInfo.getMediaServerId());
            subscribe.addSubscribe(hookSubscribe, (MediaServerItem mediaServerItemInUse, JSONObject json) -> {
                //流注册处理  发送指定mq消息
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm推流注册成功通知", "收到推流订阅消息", json.toJSONString());
                //发送调度服务的业务队列 通知流实际成功
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,StreamBusinessMsgType.STREAM_RECORD_DOWNLOAD,BusinessErrorEnums.SUCCESS,"获取流成功,开始进行录像");

                //流状态修改为成功
                // hook响应
                subscribe.removeSubscribe(hookSubscribe);
            });

            //流录像成功 回调
            HookSubscribeForRecordMp4 hookSubscribeRecord = HookSubscribeFactory.onRecordMp4(VideoManagerConstants.GB28181_APP, req.getStreamId() ,playCommonSsrcInfo.getMediaServerId());
            subscribe.addSubscribe(hookSubscribeRecord, (MediaServerItem mediaServerItemInUse, JSONObject json) -> {
                //流注册处理  发送指定mq消息
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm录像mp4成功通知", "收到录像成功返回", json.toJSONString());
                //发送调度服务的业务队列 通知流实际成功
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,StreamBusinessMsgType.STREAM_RECORD_DOWNLOAD,BusinessErrorEnums.SUCCESS,"获取流成功,开始进行录像");


                //流录像
                String filePath = json.getString("file_path");
                String streamId = json.getString("stream");
                OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(streamId);
                sendFile(filePath,oneBystreamId,2);
                //实际的录像通知完成之后  进行流删除 录像先到 还是流注销先到 这里不确定
                onlineStreamsService.remove(streamId);
                // hook响应
                subscribe.removeSubscribe(hookSubscribeRecord);
            });

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "点播回放服务", "录像流程失败", req,e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,StreamBusinessMsgType.STREAM_RECORD_DOWNLOAD,BusinessErrorEnums.UNKNOWN_ERROR,e.getMessage());
        }
    }

    private void sendFile(String fileUrl,OnlineStreamsEntity onlineStreamsEntity,int fileType){
        try {
            FileSystemResource fileResource = new FileSystemResource(fileUrl);
            // 创建请求体参数
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("alarmMsgId", onlineStreamsEntity.getUploadId());
            body.add("alarmDataType", fileType);
            RestTemplateUtil.postFile(onlineStreamsEntity.getUploadUrl(),body,null,restTemplate);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "文件上传失败", "截图流程失败", e);
        }


    }

    private static byte[] getFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            return buffer;
        }
    }
    @Override
    public void playPictureDownload(MediaPictureDownloadReq req) {
        String businessSceneKey = StreamBusinessMsgType.STREAM_PICTURE_DOWNLOAD.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+req.getStreamId();
        try {
            //阻塞型,默认是30s无返回参数
            SsrcInfo playCommonSsrcInfo = playDownloadProcess(businessSceneKey, StreamBusinessMsgType.STREAM_PICTURE_DOWNLOAD,req);
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播回放服务", "端口创建结果", playCommonSsrcInfo);
            if(ObjectUtils.isEmpty(playCommonSsrcInfo)){
                //流复用，不用通知网关
                return;
            }
            MediaPlayBackReq mediaPlayBackReq = new MediaPlayBackReq();
            BeanUtil.copyProperties(req,mediaPlayBackReq);
            mediaPlayBackReq.setStartTime(req.getTime());
            mediaPlayBackReq.setEndTime(DateUtils.getStringTimeExpireNow(req.getTime(),5));
            gatewayDealMsgService.sendGatewayPlayBackMsg(playCommonSsrcInfo,mediaPlayBackReq);

            //流注册成功 回调
            HookSubscribeForStreamChange hookSubscribe = HookSubscribeFactory.on_stream_changed(VideoManagerConstants.GB28181_APP, req.getStreamId(),true, VideoManagerConstants.GB28181_SCHEAM ,playCommonSsrcInfo.getMediaServerId());
            subscribe.addSubscribe(hookSubscribe, (MediaServerItem mediaServerItemInUse, JSONObject json) -> {
                //流注册处理  发送指定mq消息
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm推流注册成功通知", "收到推流订阅消息", json.toJSONString());
                //发送调度服务的业务队列 通知流实际成功
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,StreamBusinessMsgType.STREAM_PICTURE_DOWNLOAD,BusinessErrorEnums.SUCCESS,"获取流成功,开始进行截图");

                //进行截图 并上传
                String streamId = json.getString("stream");
                String app = json.getString("app");
                String mediaServerId = playCommonSsrcInfo.getMediaServerId();
                MediaServerItem mediaOne = mediaServerService.getOne(mediaServerId);
                String path = "snap";
                String fileName = streamId + ".jpg";
                // 请求截图
                log.info("[请求截图]: " + fileName);
                String streamUrl = String.format("rtsp://%s:%s/%s/%s", mediaOne.getIp(), mediaOne.getRtspPort(), app,  streamId);
                zlmresTfulUtils.getSnap(mediaOne, streamUrl, 5, 1, path, fileName);

                String filePath = path+ File.separator +fileName;
                OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(streamId);
                sendFile(filePath,oneBystreamId,1);

                //流状态修改为成功
                // hook响应
                subscribe.removeSubscribe(hookSubscribe);
            });

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "点播回放服务", "截图流程失败", req,e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,StreamBusinessMsgType.STREAM_PICTURE_DOWNLOAD,BusinessErrorEnums.UNKNOWN_ERROR,e.getMessage());
        }
    }

    private SsrcInfo playDownloadProcess(String businessSceneKey, StreamBusinessMsgType msgType, MediaPlayReq playReq) throws InterruptedException {
        Boolean aBoolean = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, msgType, playReq.getMsgId());
        //尝试获取锁
        if(!aBoolean){
            throw new BusinessException(BusinessErrorEnums.MEDIA_STREAM_REQUEST_TO_MANY);
        }
        String streamId = playReq.getStreamId();
        MediaServerItem oneMedia;
        // 复用流判断 针对直播场景
        //获取默认的zlm流媒体
        oneMedia =  mediaServerService.getDefaultMediaServer();
        String ssrc = "";
        if(playReq.getSsrcCheck()){
            SsrcConfig ssrcConfig = redisCatchStorageService.getSsrcConfig();
            ssrc = ssrcConfig.getPlayBackSsrc();
            //更新ssrc的缓存
            redisCatchStorageService.setSsrcConfig(ssrcConfig);
        }

        SsrcInfo ssrcInfo = mediaServerService.openRTPServer(oneMedia, playReq.getStreamId(), ssrc, playReq.getSsrcCheck(), 0);
        //流信息状态保存
        OnlineStreamsEntity onlineStreamsEntity = new OnlineStreamsEntity();
        BeanUtil.copyProperties(playReq,onlineStreamsEntity);
        onlineStreamsEntity.setMediaServerId(oneMedia.getId());
        onlineStreamsEntity.setMqExchange(playReq.getGatewayMqExchange());
        onlineStreamsEntity.setMqQueueName(playReq.getGatewayMqRouteKey());
        onlineStreamsEntity.setMqRouteKey(playReq.getGatewayMqRouteKey());
        onlineStreamsEntity.setSsrc(ssrcInfo.getSsrc());
        onlineStreamsEntity.setApp(VideoManagerConstants.GB28181_APP);
        onlineStreamsService.save(onlineStreamsEntity);
        return ssrcInfo;

    }

    private SsrcInfo playCommonProcess(String businessSceneKey, StreamBusinessMsgType msgType, MediaPlayReq playReq, boolean isPlay) throws InterruptedException {
        Boolean aBoolean = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, msgType, playReq.getMsgId());
        //尝试获取锁
        if(!aBoolean){
            throw new BusinessException(BusinessErrorEnums.MEDIA_STREAM_REQUEST_TO_MANY);
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
        onlineStreamsEntity.setMqExchange(playReq.getGatewayMqExchange());
        onlineStreamsEntity.setMqQueueName(playReq.getGatewayMqRouteKey());
        onlineStreamsEntity.setMqRouteKey(playReq.getGatewayMqRouteKey());
        onlineStreamsEntity.setSsrc(ssrcInfo.getSsrc());
        onlineStreamsEntity.setApp(VideoManagerConstants.GB28181_APP);
        onlineStreamsService.save(onlineStreamsEntity);
        return ssrcInfo;

    }

    @Override
    public void streamNotifyServer(GatewayBusinessNotifyReq gatewayBusinessNotifyReq) {
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"点播通知", JSON.toJSONString(gatewayBusinessNotifyReq));
//        String businessSceneKey = gatewayStreamNotify.getBusinessSceneKey();
//        String streamId = businessSceneKey.substring(businessSceneKey.indexOf(BusinessSceneConstants.SCENE_SEM_KEY) + 1);
        String streamId = gatewayBusinessNotifyReq.getStreamId();
        //判断点播回放
        GatewayBusinessMsgType businessMsgType = gatewayBusinessNotifyReq.getGatewayMsgType();
        String businessKey;
        StreamBusinessMsgType gatewayType = StreamBusinessMsgType.STREAM_LIVE_PLAY_START;
        if(businessMsgType.equals(GatewayBusinessMsgType.PLAY)){
            //直播
            businessKey = StreamBusinessMsgType.STREAM_LIVE_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamId;
        }else {
            businessKey = StreamBusinessMsgType.STREAM_RECORD_PLAY_START.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamId;
            gatewayType = StreamBusinessMsgType.STREAM_RECORD_PLAY_START;
        }
        BusinessErrorEnums oneBusinessNum = BusinessErrorEnums.getOneBusinessNum(gatewayBusinessNotifyReq.getCode());
        if(oneBusinessNum.equals(BusinessErrorEnums.COMMDER_SEND_SUCESS) || oneBusinessNum.equals(BusinessErrorEnums.SUCCESS)){
            //网关正常通知
        }else {
            redisCatchStorageService.editBusinessSceneKey(businessKey,gatewayType,oneBusinessNum,null);
        }

    }
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
        if(!ObjectUtils.isEmpty(oneBystreamId)){
            if(oneBystreamId.getStreamType() == 0){
                //订阅流主销处理
                //流注销成功 回调
                if(oneBystreamId.getStatus() == 0){
                    //流准备中 未成功点流
                    mediaServerService.closeRTPServer(oneBystreamId.getMediaServerId(),streamId);
                    //释放ssrc
                    redisCatchStorageService.ssrcRelease(oneBystreamId.getSsrc());
                    //清除点播请求
                    onlineStreamsService.remove(streamId);
                }else {
                    HookSubscribeForStreamChange hookSubscribe = HookSubscribeFactory.on_stream_changed(VideoManagerConstants.GB28181_APP, oneBystreamId.getStreamId(),false, VideoManagerConstants.GB28181_SCHEAM ,oneBystreamId.getMediaServerId());
                    subscribe.addSubscribe(hookSubscribe, (MediaServerItem mediaServerItemInUse, JSONObject json) -> {
                        //流注册处理  发送指定mq消息
                        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm推流注销通知", "收到推流注销消息", json.toJSONString());
                        mediaServerService.closeRTPServer(oneBystreamId.getMediaServerId(),streamId);
                        //释放ssrc
                        redisCatchStorageService.ssrcRelease(oneBystreamId.getSsrc());
                        //清除点播请求
                        if(oneBystreamId.getRecordState() == 0){
                            //录像在录像通知以后再删除
                            onlineStreamsService.remove(streamId);
                        }

                        // hook响应
                        subscribe.removeSubscribe(hookSubscribe);
                    });
                }

                //网关流注销通知
                gatewayDealMsgService.sendGatewayStreamBye(oneBystreamId,msgId,oneBystreamId);
            }else {
                //针对语音对讲下发的bye指令
                if(oneBystreamId.getMediaType() == 1){
                    gatewayDealMsgService.sendGatewayStreamBye(oneBystreamId,msgId,oneBystreamId);
                    onlineStreamsService.remove(streamId);
                }
            }
        }

    }



    @Override
    public void streamStop(String streamId, String msgId) {
        MediaServerItem defaultMediaServer = mediaServerService.getDefaultMediaServer();
        //查看流是否存在
        JSONObject rtpInfo = zlmresTfulUtils.getRtpInfo(defaultMediaServer, streamId);
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "bye之前先获取流是否存在", rtpInfo);
        CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(StreamBusinessMsgType.STREAM_PLAY_STOP.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
        mqInfo.setData(false);
        if(rtpInfo.getInteger("code") == 0){
            if (!rtpInfo.getBoolean("exist")) {
                //流不存在 通知调度中心可以关闭
                mqInfo.setData(true);
            }
        }
        //查看是否有人观看
        int i = zlmresTfulUtils.totalReaderCount(defaultMediaServer, VideoManagerConstants.GB28181_APP, streamId);
        //通知调度中心 进行bye场景的控制
        if(i>0){
            //不允许关闭
            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), dispatcherSignInConf.getMqSetQueue(), UuidUtil.toUuid(),mqInfo,true);
            return;
        }else {
            mqInfo.setData(true);
            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), dispatcherSignInConf.getMqSetQueue(), UuidUtil.toUuid(),mqInfo,true);
        }
        streamBye(streamId,msgId);

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
                            exceptionStreamBye(streamId,oneBystreamId);
                            //进行上层消息发送
                            streamCloseSend(streamId,false);
                        }

                    }else {
                        //自定义推流  针对语音对讲需要下发bye的指令
                        if(oneBystreamId.getMediaType() == 1){
                            streamCloseSend(streamId,true);

                        }else {
                            streamCloseSend(streamId,false);

                        }
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
    public synchronized void exceptionStreamBye(String streamId,OnlineStreamsEntity oneBystreamId){
        //主动管理流的关闭
        if(!ObjectUtils.isEmpty(oneBystreamId)){
            if(oneBystreamId.getStreamType() == 0){
                //订阅流主销处理
                //流注销成功 回调
                //流准备中 未成功点流
                mediaServerService.closeRTPServer(oneBystreamId.getMediaServerId(),streamId);
                //释放ssrc
                redisCatchStorageService.ssrcRelease(oneBystreamId.getSsrc());
                //清除点播请求
                if(oneBystreamId.getRecordState() == 0){
                    onlineStreamsService.remove(streamId);
                }
                //网关流注销通知
                gatewayDealMsgService.sendGatewayStreamBye(oneBystreamId,null,oneBystreamId);
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
    public void streamMediaOffline(String mediaServerId) {
        //删除全部的当前的流列表数据
        List<OnlineStreamsEntity> onlineStreamsEntities = onlineStreamsService.streamList(mediaServerId);

        if(!CollectionUtils.isEmpty(onlineStreamsEntities)){
            onlineStreamsEntities.forEach(onlineStreamsEntity -> {
                //订阅流主销处理
                //流注销成功 回调
                //释放ssrc
                redisCatchStorageService.ssrcRelease(onlineStreamsEntity.getSsrc());
                //清除点播请求
                onlineStreamsService.remove(onlineStreamsEntity.getStreamId());
                //网关流注销通知
                gatewayDealMsgService.sendGatewayStreamBye(onlineStreamsEntity,null,onlineStreamsEntity);

            });
        }
    }

    @Override
    public void streamMediaOnline(String mediaServerId) {
        MediaServerItem serverItem = mediaServerService.getOne(mediaServerId);
        List<OnlineStreamsEntity> onlineStreamsEntities = onlineStreamsService.streamList(serverItem.getId());
        if(!CollectionUtils.isEmpty(onlineStreamsEntities)){
            //查询当前流媒体中存在的流
            JSONObject rtspOnlineS = zlmresTfulUtils.getMediaListBySchema(serverItem, VideoManagerConstants.GB28181_APP, "rtsp");
            if(rtspOnlineS.getInteger("code") == 0){
                JSONArray dataArray = rtspOnlineS.getJSONArray("data");
                if(!CollectionUtils.isEmpty(dataArray)){
                    ArrayList<String> streamIdList = new ArrayList<>();
                    List<String> streamCollect = onlineStreamsEntities.stream().map(OnlineStreamsEntity::getStreamId).collect(Collectors.toList());
                    for(Object streamInfo : dataArray){
                        ZlmStreamDto zlmStreamDto = JSONObject.parseObject(streamInfo.toString(), ZlmStreamDto.class);
                        if(!streamCollect.contains(zlmStreamDto.getStream())){
                            streamIdList.add(zlmStreamDto.getStream());
                        }
                    }
                    //已经失效的播放流
                    if(!CollectionUtils.isEmpty(streamIdList)){
                        log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "zlm上线清理,已经停止的在线流",streamIdList);
                        onlineStreamsService.removeByStreamList(streamIdList);

                    }
                }else {
                    onlineStreamsService.removeAll();
                }

            }else {
                //连接失败  清理全部的流列表
                onlineStreamsService.removeAll();
            }

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
            streamCloseSend(streamId,true);
            return true;

        }else {
            return false;
        }
    }

    @Override
    public StreamInfo webRtcTalk(WebRTCTalkReq webRtcTalkReq) {
        String streamId = webRtcTalkReq.getStreamId();
        MediaServerItem oneMedia;

        OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(streamId);
        if(!ObjectUtils.isEmpty(oneBystreamId)){
            //判断流复用
            oneMedia = mediaServerService.getOne(oneBystreamId.getMediaServerId());
            if(!mediaServerService.checkRtpServer(oneMedia, streamId)){
                //流其实不存在
                onlineStreamsService.remove(streamId);

            }else{
                OnlineStreamsEntity onlineStreamsEntity = new OnlineStreamsEntity();
                BeanUtil.copyProperties(webRtcTalkReq,onlineStreamsEntity);
                onlineStreamsEntity.setMediaServerId(oneMedia.getId());
                onlineStreamsEntity.setStreamType(1);
                onlineStreamsEntity.setMediaType(1);
                onlineStreamsEntity.setMediaServerId(oneMedia.getId());
                onlineStreamsEntity.setApp(VideoManagerConstants.PUSH_LIVE_APP);
                onlineStreamsEntity.setMqExchange(webRtcTalkReq.getGatewayMqExchange());
                onlineStreamsEntity.setMqQueueName(webRtcTalkReq.getGatewayMqRouteKey());
                onlineStreamsEntity.setMqRouteKey(webRtcTalkReq.getGatewayMqRouteKey());
                onlineStreamsService.update(onlineStreamsEntity);
                return mediaServerService.getStreamInPush(oneMedia, VideoManagerConstants.GB28181_APP, streamId);
            }

        }else {
            //获取默认的zlm流媒体
            oneMedia =  mediaServerService.getDefaultMediaServer();
        }
        //webrtc推流回调
        HookSubscribeForStreamChange hookSubscribe = HookSubscribeFactory.on_stream_changed(VideoManagerConstants.PUSH_LIVE_APP, webRtcTalkReq.getStreamId(),true, VideoManagerConstants.GB28181_SCHEAM ,oneMedia.getId());
        MediaServerItem finalOneMedia = oneMedia;
        subscribe.addSubscribe(hookSubscribe, (MediaServerItem mediaServerItemInUse, JSONObject json) -> {
            //流注册处理  发送指定mq消息
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm的webrtc注册成功通知", "收到推流订阅消息", json.toJSONString());
            //通知网关进行对讲流程开启
            gatewayDealMsgService.sendGatewayWebrtcTalkMsg(webRtcTalkReq);
            //流状态修改为成功
            // hook响应
            subscribe.removeSubscribe(hookSubscribe);
        });
        dynamicTask.startDelay("webrtc:"+streamId, ()->{
            OnlineStreamsEntity oneBystreamId1 = onlineStreamsService.getOneBystreamId(streamId);
            if(ObjectUtils.isEmpty(oneBystreamId1)){
                streamCloseSend(streamId,true);
            }else {
                if(oneBystreamId1.getStatus() != 1){
                    streamCloseSend(streamId,true);
                }
            }
        },5000);
        //流信息状态保存
        OnlineStreamsEntity onlineStreamsEntity = new OnlineStreamsEntity();
        BeanUtil.copyProperties(webRtcTalkReq,onlineStreamsEntity);
        onlineStreamsEntity.setMediaServerId(oneMedia.getId());
        onlineStreamsEntity.setStreamType(1);
        onlineStreamsEntity.setMediaType(1);
        onlineStreamsEntity.setMediaServerId(oneMedia.getId());
        onlineStreamsEntity.setApp(VideoManagerConstants.PUSH_LIVE_APP);
        onlineStreamsEntity.setMqExchange(webRtcTalkReq.getGatewayMqExchange());
        onlineStreamsEntity.setMqQueueName(webRtcTalkReq.getGatewayMqRouteKey());
        onlineStreamsEntity.setMqRouteKey(webRtcTalkReq.getGatewayMqRouteKey());
        onlineStreamsService.save(onlineStreamsEntity);
        return mediaServerService.getStreamInPush(oneMedia, VideoManagerConstants.GB28181_APP, streamId);

    }
}
