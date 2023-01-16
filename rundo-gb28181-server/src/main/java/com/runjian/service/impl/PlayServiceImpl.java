package com.runjian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.CloseRtpServerDto;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.MediaServerInfoConfig;
import com.runjian.conf.SsrcConfig;
import com.runjian.conf.UserSetting;
import com.runjian.domain.dto.DeviceDto;
import com.runjian.domain.req.PlayReq;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.bean.SsrcTransaction;
import com.runjian.gb28181.session.VideoStreamSessionManager;
import com.runjian.gb28181.transmit.cmd.impl.SIPCommander;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.service.IplayService;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import javax.sip.ResponseEvent;

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

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MediaServerInfoConfig mediaServerInfoConfig;

    @Autowired
    SIPCommander sipCommander;

    @Autowired
    private VideoStreamSessionManager streamSession;

    @Override
    public void play(PlayReq playReq) {
        String businessSceneKey = GatewayMsgType.PLAY.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+playReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+playReq.getChannelId();
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            lock.lock();
            BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.PLAY,playReq.getMsgId(),userSetting.getBusinessSceneTimeout());
            boolean hset = RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, objectBusinessSceneResp);
            if(!hset){
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备点播服务", "点播失败", "redis操作hashmap失败");
                return;
            }
            //参数校验
            DeviceChannel deviceChannel = deviceChannelService.getOne(playReq.getDeviceId(), playReq.getChannelId());
            if(ObjectUtils.isEmpty(deviceChannel)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.DB_NOT_FOUND,null);
                return;
            }
            if(deviceChannel.getStatus() == 0){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.CHANNEL_OFFLINE,null);
                return;
            }
            //判断设备是否存在
            DeviceDto device = deviceService.getDevice(playReq.getDeviceId());
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return;

            }

            //判断调度服务的相关信息是否完成了初始化
            if(ObjectUtils.isEmpty(mediaServerInfoConfig)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.MEDIA_SERVER_BIND_ERROR,null);
                return;
            }

            // 复用流判断
            SsrcTransaction isPlay = streamSession.getSsrcTransaction(playReq.getDeviceId(), playReq.getChannelId(), "play", null);
            if(ObjectUtils.isEmpty(isPlay)){
                //拼接streamd的流返回值 封装返回请求体
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播成功，流已存在", playReq);

                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.SUCCESS,null);
                return;
            }
            //收流端口创建
            BaseRtpServerDto baseRtpServerDto = new BaseRtpServerDto();
            baseRtpServerDto.setDeviceId(playReq.getDeviceId());
            baseRtpServerDto.setChannelId(playReq.getChannelId());
            baseRtpServerDto.setEnableAudio(playReq.getEnableAudio());
            if(playReq.getSsrcCheck()){
                SsrcConfig ssrcConfig = redisCatchStorageService.getSsrcConfig();
                baseRtpServerDto.setSsrc(ssrcConfig.getPlaySsrc());
            }
            baseRtpServerDto.setStreamId(playReq.getDeviceId()+"_"+playReq.getChannelId());
            //todo 待定这个流程 判断观看的服务到底是哪里进行判断
            baseRtpServerDto.setMqRouteKey(mediaServerInfoConfig.getMqRoutingKey());
            CommonResponse commonResponse = RestTemplateUtil.postReturnCommonrespons(mediaServerInfoConfig.getMediaUrl() + openRtpServerApi, baseRtpServerDto, restTemplate);
            if(commonResponse.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备点播服务", "创建推流端口失败", commonResponse);

                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.MEDIA_SERVER_COLLECT_ERROR,null);
                return;
            }
            SsrcInfo ssrcInfo = (SsrcInfo)commonResponse.getData();
            if (ssrcInfo.getPort() <= 0) {
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播端口分配异常", ssrcInfo);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.MEDIA_ZLM_RTPSERVER_CREATE_ERROR,null);
                return;
            }
            //同设备进行sip交互 发送点播信令
            Device deviceBean = new Device();
            BeanUtil.copyProperties(device,deviceBean);

            sipCommander.playStreamCmd(playReq.getStreamMode(),ssrcInfo,deviceBean, playReq.getChannelId(), ok->{
                //成功业务处理
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播成功", playReq);
                //缓存当前的sip推拉流信息
                ResponseEvent responseEvent = (ResponseEvent) ok.event;
                SIPResponse response = (SIPResponse) responseEvent.getResponse();

                String contentString = new String(responseEvent.getResponse().getRawContent());
                //todo 判断ssrc是否匹配

                streamSession.putSsrcTransaction(device.getDeviceId(), playReq.getChannelId(), "play", ssrcInfo.getStreamId(), ssrcInfo.getSsrc(), ssrcInfo.getMediaServerId(), response, VideoStreamSessionManager.SessionType.play);
            },error->{
                //失败业务处理
                log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "点播服务", "点播失败", playReq);
                //关闭推流端口
                CloseRtpServerDto closeRtpServerDto = new CloseRtpServerDto();
                closeRtpServerDto.setMediaServerId(ssrcInfo.getMediaServerId());
                closeRtpServerDto.setStreamId(ssrcInfo.getStreamId());
                CommonResponse closeResponse = RestTemplateUtil.postReturnCommonrespons(mediaServerInfoConfig.getMediaUrl() + closeRtpServerApi, closeRtpServerDto, restTemplate);
                if(closeResponse.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
                    log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备点播服务", "关闭推流端口失败", commonResponse);

                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.MEDIA_SERVER_COLLECT_ERROR,null);
                    return;
                }
                //剔除缓存
                streamSession.removeSsrcTransaction(device.getDeviceId(), playReq.getChannelId(), ssrcInfo.getStreamId());
                //释放ssrc
                redisCatchStorageService.ssrcRelease(ssrcInfo.getSsrc());
            });

        }catch (Exception e){


        }

        return;


    }
}
