package com.runjian.service.impl;

import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.PlayCommonSsrcInfo;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.conf.UserSetting;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.bean.SsrcTransaction;
import com.runjian.gb28181.session.VideoStreamSessionManager;
import com.runjian.gb28181.transmit.SIPSender;
import com.runjian.gb28181.transmit.cmd.impl.SIPCommander;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.service.IplayService;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 点播相关流程
 * @author chenjialing
 */
@Service
@Slf4j
public class PlayServiceImpl implements IplayService {

    @Autowired
    IDeviceChannelService deviceChannelService;

    @Autowired
    IDeviceService deviceService;


    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;
    
    @Autowired
    UserSetting userSetting;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Value("${mdeia-api-uri-list.open-rtp-server}")
    private String openRtpServerApi;

    @Value("${mdeia-api-uri-list.close-rtp-server}")
    private String closeRtpServerApi;

    @Value("${mdeia-api-uri-list.get-rtp-info}")
    private String getRtpInfoApi;

    @Value("${mdeia-api-uri-list.stream-notify}")
    private String streamNotifyApi;

    @Autowired
    RestTemplate restTemplate;


    @Autowired
    SIPCommander sipCommander;

    @Autowired
    private SIPSender sipSender;

    @Autowired
    private VideoStreamSessionManager streamSession;

    @Value("${gateway-info.serialNum}")
    private String serialNum;


    @Autowired
    GatewaySignInConf gatewaySignInConf;


    @Override
    public void play(PlayReq playReq) {
        String businessSceneKey = GatewayBusinessMsgType.PLAY.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+playReq.getStreamId();
        try {
            //阻塞型,默认是30s无返回参数
            PlayCommonSsrcInfo playCommonSsrcInfo = playCommonProcess(businessSceneKey, GatewayBusinessMsgType.PLAY, playReq,true);
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "端口创建结果", playCommonSsrcInfo);
            if(ObjectUtils.isEmpty(playCommonSsrcInfo)){
                return;
            }
            SsrcInfo ssrcInfo = playCommonSsrcInfo.getSsrcInfo();
            Device device = new Device();
            device.setHostAddress(playCommonSsrcInfo.getHostAddress());
            device.setTransport(playCommonSsrcInfo.getTransport());
            device.setDeviceId(playCommonSsrcInfo.getDeviceId());
            sipCommander.playStreamCmd(playReq.getStreamMode(),ssrcInfo,device, playReq.getChannelId(), ok->{
                //成功业务处理
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播成功", playReq);
                //缓存当前的sip推拉流信息
                ResponseEvent responseEvent = (ResponseEvent) ok.event;
                SIPResponse response = (SIPResponse) responseEvent.getResponse();

                String contentString = new String(responseEvent.getResponse().getRawContent());
                //todo 判断ssrc是否匹配

                //传递ssrc进去，出现推流不成功的异常，进行相关逻辑处理
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.PLAY,BusinessErrorEnums.COMMDER_SEND_SUCESS,playReq);
                streamSession.putSsrcTransaction(device.getDeviceId(), playReq.getChannelId(), "play", playReq.getStreamId(), ssrcInfo.getSsrc(), ssrcInfo.getMediaServerId(), response, VideoStreamSessionManager.SessionType.play,playReq.getDispatchUrl());
            },error->{
                //失败业务处理
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播失败", playReq);
                //关闭推流端口
                streamSession.removeSsrcTransaction(device.getDeviceId(), playReq.getChannelId(), playReq.getStreamId());

                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.PLAY,BusinessErrorEnums.SIP_SEND_EXCEPTION,playReq);
            });

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "点播服务", "点播失败", playReq,e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.PLAY,BusinessErrorEnums.UNKNOWN_ERROR,playReq);
        }



    }

    @Override
    public void playBack(PlayBackReq playBackReq) {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "回放服务", "点播请求进入", playBackReq);
        String businessSceneKey = GatewayBusinessMsgType.PLAY_BACK.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+playBackReq.getStreamId();
        try {
            PlayCommonSsrcInfo playCommonSsrcInfo = playCommonProcess(businessSceneKey, GatewayBusinessMsgType.PLAY_BACK, playBackReq,false);
            if(ObjectUtils.isEmpty(playCommonSsrcInfo)){
                return;
            }
            SsrcInfo ssrcInfo = playCommonSsrcInfo.getSsrcInfo();
            Device device = new Device();
            device.setHostAddress(playCommonSsrcInfo.getHostAddress());
            device.setTransport(playCommonSsrcInfo.getTransport());
            device.setDeviceId(playCommonSsrcInfo.getDeviceId());
            sipCommander.playbackStreamCmd(playBackReq.getStreamMode(),ssrcInfo,device, playBackReq.getChannelId(),playBackReq.getStartTime(),playBackReq.getEndTime(), ok->{
                //成功业务处理
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "回放服务", "点播成功", playBackReq);
                //缓存当前的sip推拉流信息
                ResponseEvent responseEvent = (ResponseEvent) ok.event;
                SIPResponse response = (SIPResponse) responseEvent.getResponse();

                String contentString = new String(responseEvent.getResponse().getRawContent());
                //todo 判断ssrc是否匹配

                //传递ssrc进去，出现推流不成功的异常，进行相关逻辑处理
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.PLAY_BACK,BusinessErrorEnums.COMMDER_SEND_SUCESS,playBackReq);
                streamSession.putSsrcTransaction(device.getDeviceId(), playBackReq.getChannelId(), sipSender.getNewCallIdHeader(device.getTransport()).getCallId(), playBackReq.getStreamId(), ssrcInfo.getSsrc(), ssrcInfo.getMediaServerId(), response, VideoStreamSessionManager.SessionType.playback,playBackReq.getDispatchUrl());
            },error->{
                //失败业务处理
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "回放服务", "点播失败", playBackReq);
                //剔除缓存
                streamSession.removeSsrcTransaction(device.getDeviceId(), playBackReq.getChannelId(), playBackReq.getStreamId());

                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.PLAY_BACK,BusinessErrorEnums.SIP_SEND_EXCEPTION,playBackReq);
            });
        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "回放服务", "点播失败", playBackReq);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.PLAY_BACK,BusinessErrorEnums.UNKNOWN_ERROR,playBackReq);
        }
    }

    private PlayCommonSsrcInfo playCommonProcess(String businessSceneKey, GatewayBusinessMsgType gatewayMsgType, PlayReq playReq,boolean isPlay) throws InterruptedException {
        redisCatchStorageService.addBusinessSceneKey(businessSceneKey,gatewayMsgType,playReq.getMsgId());
        //尝试获取锁
        RLock lock = redissonClient.getLock(businessSceneKey);
        boolean b = lock.tryLock(0,userSetting.getBusinessSceneTimeout()+100, TimeUnit.MILLISECONDS);
        if(!b){
            //加锁失败，不继续执行
            log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备点播服务,加锁失败，合并全局的请求",playReq);
            return null;
        }
        //参数校验
        DeviceChannel deviceChannel = deviceChannelService.getOne(playReq.getDeviceId(), playReq.getChannelId());
        if(ObjectUtils.isEmpty(deviceChannel)){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.DB_NOT_FOUND,playReq);
            return null;
        }
        if(deviceChannel.getStatus() == 0){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.CHANNEL_OFFLINE,playReq);
            return null;
        }
        //判断设备是否存在
        Device device = deviceService.getDevice(playReq.getDeviceId());
        if(ObjectUtils.isEmpty(device)){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,playReq);
            return null;

        }

        PlayCommonSsrcInfo playCommonSsrcInfo = new PlayCommonSsrcInfo();
        playCommonSsrcInfo.setHostAddress(device.getHostAddress());
        playCommonSsrcInfo.setTransport(device.getTransport());
        playCommonSsrcInfo.setDeviceId(device.getDeviceId());
        playCommonSsrcInfo.setSsrcInfo(playReq.getSsrcInfo());
        return playCommonSsrcInfo;
    }
    /**
     * 流注册事件，修改业务状态
     * @param streamInfo
     */
    @Override
    public void onStreamChanges(StreamInfo streamInfo) {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "流注册事件", streamInfo);

//        //获取点播的ssrc信息值
//        SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamInfo.getStreamId());
//        if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
//            //拼接streamd的流返回值 封装返回请求体
//            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "流注册失败，当前点播信息的缓存获取异常", streamInfo);
//
//            return;
//        }
//        String businessSceneKey = null;
//        //判断类型
//        if(streamSessionSsrcTransaction.getType().equals(VideoStreamSessionManager.SessionType.play)){
//            businessSceneKey = GatewayMsgType.PLAY.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamSessionSsrcTransaction.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+streamSessionSsrcTransaction.getChannelId();
//            //点播成功
//            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.SUCCESS,streamInfo);
//        }else if(streamSessionSsrcTransaction.getType().equals(VideoStreamSessionManager.SessionType.playback)){
//            businessSceneKey = GatewayMsgType.PLAY_BACK.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamSessionSsrcTransaction.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+streamSessionSsrcTransaction.getChannelId();
//            //点播成功
//            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY_BACK,BusinessErrorEnums.SUCCESS,streamInfo);
//        }


    }

    @Override
    public void onStreamNoneReader(NoneStreamReaderReq noneStreamReaderReq) {
        //todo 获取无人观看的信息，暂时全部进行设备的推流关闭,不进行平台能力层信息上报
//        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"无人观看请求进入",noneStreamReaderReq);
//        if(VideoManagerConstants.GB28181_APP.equals(noneStreamReaderReq.getApp())){
//            //进行国标流程的处理
//            String streamId = noneStreamReaderReq.getStreamId();
//            SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamId);
//            if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
//                //todo 重要，缓存异常，点播失败需要人工介入
//                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,点播缓存异常", streamId);
//                return;
//            }
//
//            Device device = deviceService.getDevice(streamSessionSsrcTransaction.getDeviceId());
//            if(ObjectUtils.isEmpty(device)){
//                //todo 重要，缓存异常，点播失败需要人工介入
//                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,设备信息未找到", streamId);
//                return;
//
//            }
//            try {
//                sipCommander.streamByeCmd(streamSessionSsrcTransaction,device,streamSessionSsrcTransaction.getChannelId(),error->{
//                    //todo 重要，点播失败 后续需要具体分析为啥失败，针对直播bye失败需要重点关注，回放bye失败需要排查看一下
//                    ResponseEvent responseEvent = (ResponseEvent) error.event;
//                    SIPResponse response = (SIPResponse) responseEvent.getResponse();
//                    log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "bye指令点播失败", response);
//
//                },ok->{
//
//                    //剔除缓存
//                    streamSession.removeSsrcTransaction(streamSessionSsrcTransaction);
//                });
//            } catch (InvalidArgumentException | SipException | ParseException e) {
//                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "国标设备点播", "[命令发送失败] 停止点播， 发送BYE", e);
//
//            }
//
//
//        }else {
//            //非国标的无人观看 暂时不处理
//        }

    }

    @Async("taskExecutor")
    @Override
    public void playBusinessErrorScene(String businessKey,BusinessSceneResp businessSceneResp) {
        //点播相关的key的组合条件
        //String businessSceneKey = GatewayMsgType.PLAY.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+playReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY+playReq.getChannelId();

//        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播失败，异常处理流程", businessSceneResp);
//        //处理sip交互成功，但是流注册未返回的情况
//
//        //获取businessKey的deviceId,和channelId
//        int deviceStart = businessKey.indexOf(BusinessSceneConstants.SCENE_SEM_KEY);
//        int channelStart = businessKey.indexOf(BusinessSceneConstants.SCENE_STREAM_KEY);
//
//        Object data = businessSceneResp.getData();
//        if(ObjectUtils.isEmpty(data)){
//            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "错误点播场景处理失败,缓存信息异常", businessSceneResp);
//        }
//        //判断缓存是否存在
//        //1.自行释放ssrc和2.删除相关的缓存，3.设备指令的bye和4.流媒体推流端口的关闭
//        SsrcInfo ssrcInfo =  JSONObject.toJavaObject((JSONObject)businessSceneResp.getData(),SsrcInfo.class);
//        SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, ssrcInfo.getStreamId());
//        if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
//            //todo 重要，缓存异常，点播失败需要人工介入
//            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "错误点播场景处理失败,点播缓存异常", businessSceneResp);
//            return;
//        }
//        String deviceId = streamSessionSsrcTransaction.getDeviceId();
//        String channelId =streamSessionSsrcTransaction.getChannelId();
//        Device device = deviceService.getDevice(deviceId);
//        if(ObjectUtils.isEmpty(device)){
//            //todo 重要，缓存异常，点播失败需要人工介入
//            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "错误点播场景处理失败,设备信息未找到", businessSceneResp);
//        }
//
//
//        //设备指令 bye
//        try {
//            sipCommander.streamByeCmd(streamSessionSsrcTransaction,device,channelId,error->{
//                //todo 重要，点播失败 后续需要具体分析为啥失败，针对直播bye失败需要重点关注，回放bye失败需要排查看一下
//                ResponseEvent responseEvent = (ResponseEvent) error.event;
//                SIPResponse response = (SIPResponse) responseEvent.getResponse();
//                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "bye指令点播失败", response);
//
//            },ok->{
//                ResponseEvent responseEvent = (ResponseEvent) ok.event;
//                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "bye指令成功", responseEvent.getResponse());
//                //剔除缓存
//                streamSession.removeSsrcTransaction(streamSessionSsrcTransaction);
//            });
//        } catch (InvalidArgumentException | SipException | ParseException e) {
//            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "国标设备点播", "[命令发送失败] 停止点播， 发送BYE", e);
//
//        }



    }


    @Override
    public void streamBye(String streamId,String msgId) {
        log.info(LogTemplate.ERROR_LOG_TEMPLATE, "停止点播", "流停止请求进入， 发送BYE", streamId);
        SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamId);
        if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
            //todo 重要，缓存异常，点播失败需要人工介入
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,点播缓存异常", streamId);
            return;
        }

            //设备指令 bye
        try {
            Device device = deviceService.getDevice(streamSessionSsrcTransaction.getDeviceId());
            if(ObjectUtils.isEmpty(device)){
                //todo 重要，缓存异常，点播失败需要人工介入
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,设备信息未找到", streamId);
                return;

            }
            sipCommander.streamByeCmd(streamSessionSsrcTransaction,device,streamSessionSsrcTransaction.getChannelId(),error->{
                //todo 重要，点播失败 后续需要具体分析为啥失败，针对直播bye失败需要重点关注，回放bye失败需要排查看一下
                ResponseEvent responseEvent = (ResponseEvent) error.event;
                SIPResponse response = (SIPResponse) responseEvent.getResponse();
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "bye指令点播失败", response);

            },ok->{

            });

            //剔除缓存
            streamSession.removeSsrcTransaction(streamSessionSsrcTransaction);

        } catch (InvalidArgumentException | SipException | ParseException e) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "停止点播", "[命令发送失败] 停止点播， 发送BYE", e);
        }


    }

    @Override
    public Boolean testStreamBye(String streamId, String callId) {
        log.info(LogTemplate.ERROR_LOG_TEMPLATE, "test停止点播", "流停止请求进入， 发送BYE", streamId);
         AtomicReference<Boolean> flag = new AtomicReference<>(false);
        SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, callId, streamId);
        if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
            //todo 重要，缓存异常，点播失败需要人工介入
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,点播缓存异常", streamId);
            return false;
        }

        //设备指令 bye
        try {
            Device device = deviceService.getDevice(streamSessionSsrcTransaction.getDeviceId());
            if(ObjectUtils.isEmpty(device)){
                //todo 重要，缓存异常，点播失败需要人工介入
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,设备信息未找到", streamId);
                return false;

            }
            sipCommander.streamByeCmd(streamSessionSsrcTransaction,device,streamSessionSsrcTransaction.getChannelId(),error->{
                //todo 重要，点播失败 后续需要具体分析为啥失败，针对直播bye失败需要重点关注，回放bye失败需要排查看一下
                ResponseEvent responseEvent = (ResponseEvent) error.event;
                SIPResponse response = (SIPResponse) responseEvent.getResponse();
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "bye指令点播失败", response);
                flag.set(false);
            },ok->{
                flag.set(true);
            });

            //剔除缓存
            streamSession.removeSsrcTransaction(streamSessionSsrcTransaction);

        } catch (InvalidArgumentException | SipException | ParseException e) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "停止点播", "[命令发送失败] 停止点播， 发送BYE", e);
        }
        return flag.get();
    }

    @Override
    public void playSpeedControl(String streamId, Double speed, String msgId) {
        //指令型操作 无需加redisson的锁
        try {

            SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamId);
            if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "倍速操作", "倍速操作--失败，流不存在", streamId);
                return;
            }
            String deviceId = streamSessionSsrcTransaction.getDeviceId();
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "倍速操作", "倍速操作--失败，设备信息不存在", streamId);
                return;
            }
            sipCommander.playSpeedCmd(device,streamSessionSsrcTransaction,speed);
        }catch(Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "倍速操作", "倍速操作--失败，未知异常", streamId);
            return;
        }

    }

    @Override
    public void playPauseControl(String streamId, String msgId) {
        //指令型操作 无需加redisson的锁
        try {
            //阻塞型,默认是30s无返回参数

            SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamId);
            if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "暂停操作", "暂停操作--失败，流不存在", streamId);
                return;
            }
            String deviceId = streamSessionSsrcTransaction.getDeviceId();
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "暂停操作", "暂停操作--失败，设备信息不存在", streamId);
                return;
            }
            sipCommander.playPauseCmd(device,streamSessionSsrcTransaction);
        }catch(Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "暂停操作", "暂停操作--失败，未知异常", streamId);
            return;
        }
    }

    @Override
    public void playResumeControl(String streamId, String msgId) {
//指令型操作 无需加redisson的锁
        try {

            SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamId);
            if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
                //todo 重要，缓存异常，点播失败需要人工介入
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "录像恢复操作", "录像恢复操作--失败，流不存在", streamId);
                return;
            }
            String deviceId = streamSessionSsrcTransaction.getDeviceId();
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "录像恢复操作", "录像恢复操作--失败，设备信息不存在", streamId);
                return;
            }
            sipCommander.playResumeCmd(device,streamSessionSsrcTransaction);
        }catch(Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "录像恢复操作", "录像恢复操作--失败，未知异常", streamId);
            return;
        }
    }

    @Override
    public void playSeekControl(String streamId, long seekTime, String msgId) {
        //指令型操作 无需加redisson的锁
        try {
            //阻塞型,默认是30s无返回参数

            SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamId);
            if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
                //todo 重要，缓存异常，点播失败需要人工介入
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "录像定点操作", "录像定点操作--失败，流不存在", streamId);
                return;
            }
            String deviceId = streamSessionSsrcTransaction.getDeviceId();
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "录像定点操作", "录像定点操作--失败，设备信息不存在", streamId);
                return;
            }
            sipCommander.playSeekCmd(device,streamSessionSsrcTransaction,seekTime);
        }catch(Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "录像定点操作", "录像定点操作--失败，未知异常", streamId);
            return;
        }
    }
}
