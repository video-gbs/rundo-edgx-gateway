package com.runjian.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.GatewayBindMedia;
import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.CloseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.RtpInfoDto;
import com.runjian.common.commonDto.PlayCommonSsrcInfo;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.SsrcConfig;
import com.runjian.conf.UserSetting;
import com.runjian.domain.dto.DeviceDto;
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



    @Override
    public void play(PlayReq playReq) {
        String businessSceneKey = GatewayMsgType.PLAY.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+playReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+playReq.getChannelId();
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            lock.lock();
            PlayCommonSsrcInfo playCommonSsrcInfo = playCommonProcess(businessSceneKey, GatewayMsgType.PLAY, playReq,true);
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
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.SIP_SEND_SUCESS,ssrcInfo);
                streamSession.putSsrcTransaction(device.getDeviceId(), playReq.getChannelId(), "play", ssrcInfo.getStreamId(), ssrcInfo.getSsrc(), ssrcInfo.getMediaServerId(), response, VideoStreamSessionManager.SessionType.play);
            },error->{
                //失败业务处理
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播失败", playReq);
                //关闭推流端口
                closeGb28181RtpServer(ssrcInfo.getStreamId(),ssrcInfo.getMediaServerId());
                //剔除缓存
                streamSession.removeSsrcTransaction(device.getDeviceId(), playReq.getChannelId(), ssrcInfo.getStreamId());
                //释放ssrc
                redisCatchStorageService.ssrcRelease(ssrcInfo.getSsrc());

                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.SIP_SEND_EXCEPTION,null);
            });

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "点播服务", "点播失败", playReq,e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }

        return;


    }

    @Override
    public void playBack(PlayBackReq playBackReq) {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "回放服务", "点播请求进入", playBackReq);
        String businessSceneKey = GatewayMsgType.PLAY_BACK.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+playBackReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+playBackReq.getChannelId();
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            lock.lock();
            PlayCommonSsrcInfo playCommonSsrcInfo = playCommonProcess(businessSceneKey, GatewayMsgType.PLAY_BACK, playBackReq,false);
            if(ObjectUtils.isEmpty(playCommonSsrcInfo)){
                return;
            }
            SsrcInfo ssrcInfo = playCommonSsrcInfo.getSsrcInfo();
            Device device = new Device();
            device.setHostAddress(playCommonSsrcInfo.getHostAddress());
            device.setTransport(playCommonSsrcInfo.getTransport());
            device.setDeviceId(playCommonSsrcInfo.getDeviceId());
            sipCommander.playStreamCmd(playBackReq.getStreamMode(),ssrcInfo,device, playBackReq.getChannelId(), ok->{
                //成功业务处理
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "回放服务", "点播成功", playBackReq);
                //缓存当前的sip推拉流信息
                ResponseEvent responseEvent = (ResponseEvent) ok.event;
                SIPResponse response = (SIPResponse) responseEvent.getResponse();

                String contentString = new String(responseEvent.getResponse().getRawContent());
                //todo 判断ssrc是否匹配

                //传递ssrc进去，出现推流不成功的异常，进行相关逻辑处理
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.SIP_SEND_SUCESS,ssrcInfo);
                streamSession.putSsrcTransaction(device.getDeviceId(), playBackReq.getChannelId(), sipSender.getNewCallIdHeader(device.getTransport()).getCallId(), ssrcInfo.getStreamId(), ssrcInfo.getSsrc(), ssrcInfo.getMediaServerId(), response, VideoStreamSessionManager.SessionType.playback);
            },error->{
                //失败业务处理
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "回放服务", "点播失败", playBackReq);
                //关闭推流端口
                closeGb28181RtpServer(ssrcInfo.getStreamId(),ssrcInfo.getMediaServerId());
                //剔除缓存
                streamSession.removeSsrcTransaction(device.getDeviceId(), playBackReq.getChannelId(), ssrcInfo.getStreamId());
                //释放ssrc
                redisCatchStorageService.ssrcRelease(ssrcInfo.getSsrc());

                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.SIP_SEND_EXCEPTION,null);
            });
        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "回放服务", "点播失败", playBackReq);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }


        return;
    }

    private PlayCommonSsrcInfo playCommonProcess(String businessSceneKey, GatewayMsgType gatewayMsgType, PlayReq playReq,boolean isPlay){

        BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(gatewayMsgType,playReq.getMsgId(),userSetting.getBusinessSceneTimeout());
        boolean hset = RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, objectBusinessSceneResp);
        if(!hset){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备点播服务", "点播失败", "redis操作hashmap失败");
            return null;
        }
        //参数校验
        DeviceChannel deviceChannel = deviceChannelService.getOne(playReq.getDeviceId(), playReq.getChannelId());
        if(ObjectUtils.isEmpty(deviceChannel)){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.DB_NOT_FOUND,null);
            return null;
        }
        if(deviceChannel.getStatus() == 0){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.CHANNEL_OFFLINE,null);
            return null;
        }
        //判断设备是否存在
        DeviceDto device = deviceService.getDevice(playReq.getDeviceId());
        if(ObjectUtils.isEmpty(device)){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
            return null;

        }
        GatewayBindMedia gatewayBindMedia =  (GatewayBindMedia)RedisCommonUtil.get(redisTemplate,BusinessSceneConstants.BIND_GATEWAY_MEDIA+serialNum);
        //判断调度服务的相关信息是否完成了初始化
        if(ObjectUtils.isEmpty(gatewayBindMedia)){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.MEDIA_SERVER_BIND_ERROR,null);
            return null;
        }

        // 复用流判断 针对直播场景
        if(isPlay){
            SsrcTransaction ssrcTransaction = streamSession.getSsrcTransaction(playReq.getDeviceId(), playReq.getChannelId(), "play", null);
            if(!ObjectUtils.isEmpty(ssrcTransaction)){
                //拼接streamd的流返回值 封装返回请求体
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播成功，流已存在", playReq);
                //进行流媒体中流的判断
                RtpInfoDto rtpInfoDto = new RtpInfoDto();
                rtpInfoDto.setApp(VideoManagerConstants.GB28181_APP);
                rtpInfoDto.setMediaServerId(ssrcTransaction.getMediaServerId());
                rtpInfoDto.setStreamId(ssrcTransaction.getStream());
                CommonResponse commonResponse = RestTemplateUtil.postReturnCommonrespons(gatewayBindMedia.getUrl() + getRtpInfoApi, rtpInfoDto, restTemplate);
                if(commonResponse.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
                    log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备点播服务", "zlm连接失败", commonResponse);

                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.MEDIA_SERVER_COLLECT_ERROR,null);
                    return null;
                }else {
                    if(ObjectUtils.isEmpty(commonResponse.getData())){
                        //流实际已经不存在 ，接着点播即可
                        streamSession.removeSsrcTransaction(ssrcTransaction);
                    }else {
                        //流存在 直接返回流封装的地址信息
                        redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.SUCCESS,commonResponse.getData());
                        return null;

                    }
                }

            }
        }

        //收流端口创建
        BaseRtpServerDto baseRtpServerDto = new BaseRtpServerDto();
        baseRtpServerDto.setDeviceId(playReq.getDeviceId());
        baseRtpServerDto.setChannelId(playReq.getChannelId());
        baseRtpServerDto.setEnableAudio(playReq.getEnableAudio());
        baseRtpServerDto.setGatewayId(serialNum);
        if(playReq.getSsrcCheck()){
            SsrcConfig ssrcConfig = redisCatchStorageService.getSsrcConfig();
            if(isPlay){
                baseRtpServerDto.setSsrc(ssrcConfig.getPlaySsrc());
                baseRtpServerDto.setStreamId(playReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY+playReq.getChannelId());

            }else {
                baseRtpServerDto.setSsrc(ssrcConfig.getPlayBackSsrc());
                String streamId = String.format("%08x", Integer.parseInt(baseRtpServerDto.getSsrc())).toUpperCase();
                baseRtpServerDto.setStreamId(streamId);
            }
            //更新ssrc的缓存
            redisCatchStorageService.setSsrcConfig(ssrcConfig);
        }
        //todo 待定这个流程 判断观看的服务到底是哪里进行判断
        CommonResponse commonResponse = RestTemplateUtil.postReturnCommonrespons(gatewayBindMedia.getUrl() + openRtpServerApi, baseRtpServerDto, restTemplate);
        if(commonResponse.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备点播服务", "创建推流端口失败", commonResponse);

            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.MEDIA_SERVER_COLLECT_ERROR,null);
            return null;
        }
        //返回数据为json 进行组装

        SsrcInfo ssrcInfo = (SsrcInfo) commonResponse.getData();

        if (ssrcInfo.getPort() <= 0) {
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播端口分配异常", ssrcInfo);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,gatewayMsgType,BusinessErrorEnums.MEDIA_ZLM_RTPSERVER_CREATE_ERROR,null);
            return null;
        }

        PlayCommonSsrcInfo playCommonSsrcInfo = new PlayCommonSsrcInfo();
        playCommonSsrcInfo.setHostAddress(device.getHostAddress());
        playCommonSsrcInfo.setTransport(device.getTransport());
        playCommonSsrcInfo.setDeviceId(device.getDeviceId());

        playCommonSsrcInfo.setSsrcInfo(ssrcInfo);

        return playCommonSsrcInfo;
    }
    /**
     * 流注册事件，修改业务状态
     * @param streamInfo
     */
    @Override
    public void onStreamChanges(StreamInfo streamInfo) {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "流注册事件", streamInfo);

        //获取点播的ssrc信息值
        SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamInfo.getStreamId());
        if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
            //拼接streamd的流返回值 封装返回请求体
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "流注册失败，当前点播信息的缓存获取异常", streamInfo);

            return;
        }
        String businessSceneKey = null;
        //判断类型
        if(streamSessionSsrcTransaction.getType().equals(VideoStreamSessionManager.SessionType.play)){
            businessSceneKey = GatewayMsgType.PLAY.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamSessionSsrcTransaction.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+streamSessionSsrcTransaction.getChannelId();
            //点播成功
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.SUCCESS,streamInfo);
        }else if(streamSessionSsrcTransaction.getType().equals(VideoStreamSessionManager.SessionType.playback)){
            businessSceneKey = GatewayMsgType.PLAY_BACK.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamSessionSsrcTransaction.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+streamSessionSsrcTransaction.getChannelId();
            //点播成功
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY_BACK,BusinessErrorEnums.SUCCESS,streamInfo);
        }


    }

    @Override
    public void onStreamNoneReader(NoneStreamReaderReq noneStreamReaderReq) {
        //todo 获取无人观看的信息，暂时全部进行设备的推流关闭,不进行平台能力层信息上报
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"无人观看请求进入",noneStreamReaderReq);
        if(VideoManagerConstants.GB28181_APP.equals(noneStreamReaderReq.getApp())){
            //进行国标流程的处理
            String streamId = noneStreamReaderReq.getStreamId();
            SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamId);
            if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
                //todo 重要，缓存异常，点播失败需要人工介入
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,点播缓存异常", streamId);
                return;
            }

            DeviceDto deviceDto = deviceService.getDevice(streamSessionSsrcTransaction.getDeviceId());
            if(ObjectUtils.isEmpty(deviceDto)){
                //todo 重要，缓存异常，点播失败需要人工介入
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,设备信息未找到", streamId);
                return;

            }
            Device device = new Device();
            BeanUtil.copyProperties(deviceDto,device);
            try {
                sipCommander.streamByeCmd(streamSessionSsrcTransaction,device,streamSessionSsrcTransaction.getChannelId(),error->{
                    //todo 重要，点播失败 后续需要具体分析为啥失败，针对直播bye失败需要重点关注，回放bye失败需要排查看一下
                    ResponseEvent responseEvent = (ResponseEvent) error.event;
                    SIPResponse response = (SIPResponse) responseEvent.getResponse();
                    log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "bye指令点播失败", response);

                });
            } catch (InvalidArgumentException | SipException | ParseException e) {
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "国标设备点播", "[命令发送失败] 停止点播， 发送BYE", e);

            }

            //释放ssrc
            redisCatchStorageService.ssrcRelease(streamSessionSsrcTransaction.getSsrc());

            //关闭推流端口
            closeGb28181RtpServer(streamSessionSsrcTransaction.getStream(),streamSessionSsrcTransaction.getMediaServerId());
            //剔除缓存
            streamSession.removeSsrcTransaction(streamSessionSsrcTransaction);
        }else {
            //非国标的无人观看 暂时不处理
        }

    }

    @Async("taskExecutor")
    @Override
    public void playBusinessErrorScene(String businessKey,BusinessSceneResp businessSceneResp) {
        //点播相关的key的组合条件
        //String businessSceneKey = GatewayMsgType.PLAY.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+playReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY+playReq.getChannelId();

        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播失败，异常处理流程", businessSceneResp);
        //处理sip交互成功，但是流注册未返回的情况

        //获取businessKey的deviceId,和channelId
        int deviceStart = businessKey.indexOf(BusinessSceneConstants.SCENE_SEM_KEY);
        int channelStart = businessKey.indexOf(BusinessSceneConstants.SCENE_STREAM_KEY);

        Object data = businessSceneResp.getData();
        if(ObjectUtils.isEmpty(data)){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "错误点播场景处理失败,缓存信息异常", businessSceneResp);
        }
        //判断缓存是否存在
        //1.自行释放ssrc和2.删除相关的缓存，3.设备指令的bye和4.流媒体推流端口的关闭
        SsrcInfo ssrcInfo =  JSONObject.toJavaObject((JSONObject)businessSceneResp.getData(),SsrcInfo.class);
        SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, ssrcInfo.getStreamId());
        if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
            //todo 重要，缓存异常，点播失败需要人工介入
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "错误点播场景处理失败,点播缓存异常", businessSceneResp);
            return;
        }
        String deviceId = streamSessionSsrcTransaction.getDeviceId();
        String channelId =streamSessionSsrcTransaction.getChannelId();
        DeviceDto deviceDto = deviceService.getDevice(deviceId);
        if(ObjectUtils.isEmpty(deviceDto)){
            //todo 重要，缓存异常，点播失败需要人工介入
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "错误点播场景处理失败,设备信息未找到", businessSceneResp);
        }
        Device device = new Device();
        BeanUtil.copyProperties(deviceDto,device);

        //释放ssrc
        redisCatchStorageService.ssrcRelease(ssrcInfo.getSsrc());
        //设备指令 bye
        try {
            sipCommander.streamByeCmd(streamSessionSsrcTransaction,device,channelId,error->{
                //todo 重要，点播失败 后续需要具体分析为啥失败，针对直播bye失败需要重点关注，回放bye失败需要排查看一下
                ResponseEvent responseEvent = (ResponseEvent) error.event;
                SIPResponse response = (SIPResponse) responseEvent.getResponse();
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "bye指令点播失败", response);

            });
        } catch (InvalidArgumentException | SipException | ParseException e) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "国标设备点播", "[命令发送失败] 停止点播， 发送BYE", e);

        }
        //关闭推流端口
        closeGb28181RtpServer(ssrcInfo.getStreamId(),ssrcInfo.getMediaServerId());
        //剔除缓存
        streamSession.removeSsrcTransaction(streamSessionSsrcTransaction);


    }

    private void closeGb28181RtpServer(String streamId,String mediaServerId) {
        CloseRtpServerDto closeRtpServerDto = new CloseRtpServerDto();
        closeRtpServerDto.setMediaServerId(mediaServerId);
        closeRtpServerDto.setStreamId(streamId);
        GatewayBindMedia gatewayBindMedia =  (GatewayBindMedia)RedisCommonUtil.get(redisTemplate,BusinessSceneConstants.BIND_GATEWAY_MEDIA+serialNum);
        CommonResponse closeResponse = RestTemplateUtil.postCloseRtpserverRespons(gatewayBindMedia.getUrl() + closeRtpServerApi, closeRtpServerDto, restTemplate);
        if(closeResponse.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
            //后续这zlm也会进行关闭该端口
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备点播服务", "关闭推流端口失败", closeResponse);
        }
    }

    @Override
    public void streamBye(String streamId,String msgId) {
        SsrcTransaction streamSessionSsrcTransaction = streamSession.getSsrcTransaction(null, null, null, streamId);
        if(ObjectUtils.isEmpty(streamSessionSsrcTransaction)){
            //todo 重要，缓存异常，点播失败需要人工介入
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,点播缓存异常", streamId);
            return;
        }

        String businessSceneKey = GatewayMsgType.STOP_PLAY.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamSessionSsrcTransaction.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+streamSessionSsrcTransaction.getChannelId();
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            lock.lock();
            BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.STOP_PLAY,msgId,userSetting.getBusinessSceneTimeout());
            boolean hset = RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, objectBusinessSceneResp);
            if(!hset){
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "停止点播", "点播失败", "redis操作hashmap失败");
                return;
            }
            //设备指令 bye
            try {
                DeviceDto deviceDto = deviceService.getDevice(streamSessionSsrcTransaction.getDeviceId());
                if(ObjectUtils.isEmpty(deviceDto)){
                    //todo 重要，缓存异常，点播失败需要人工介入
                    log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "错误点播场景处理失败,设备信息未找到", streamId);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.STOP_PLAY,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                    return;

                }
                Device device = new Device();
                BeanUtil.copyProperties(deviceDto,device);
                sipCommander.streamByeCmd(streamSessionSsrcTransaction,device,streamSessionSsrcTransaction.getChannelId(),error->{
                    //todo 重要，点播失败 后续需要具体分析为啥失败，针对直播bye失败需要重点关注，回放bye失败需要排查看一下
                    ResponseEvent responseEvent = (ResponseEvent) error.event;
                    SIPResponse response = (SIPResponse) responseEvent.getResponse();
                    log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "bye指令点播失败", response);

                });

                //释放ssrc
                redisCatchStorageService.ssrcRelease(streamSessionSsrcTransaction.getSsrc());

                //关闭推流端口
                closeGb28181RtpServer(streamSessionSsrcTransaction.getStream(),streamSessionSsrcTransaction.getMediaServerId());
                //剔除缓存
                streamSession.removeSsrcTransaction(streamSessionSsrcTransaction);
            } catch (InvalidArgumentException | SipException | ParseException e) {
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "停止点播", "[命令发送失败] 停止点播， 发送BYE", e);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.STOP_PLAY,BusinessErrorEnums.SIP_SEND_EXCEPTION,null);
            }
        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "停止点播", "停止点播失败", streamId);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.STOP_PLAY,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }



    }
}
