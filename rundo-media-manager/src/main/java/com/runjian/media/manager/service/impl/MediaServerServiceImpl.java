package com.runjian.media.manager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.runjian.common.constant.LogTemplate;
import com.runjian.media.manager.conf.DynamicTask;
import com.runjian.media.manager.conf.MediaConfig;
import com.runjian.media.manager.conf.UserSetting;
import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
import com.runjian.media.manager.event.MediaEventPublisher;
import com.runjian.media.manager.service.IMediaRestfulApiService;
import com.runjian.media.manager.service.IMediaServerService;
import com.runjian.media.manager.storager.MediaServerRedisStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class MediaServerServiceImpl  implements IMediaServerService {

    @Autowired
    MediaConfig mediaConfig;

    @Autowired
    IMediaRestfulApiService iMediaRestfulApiService;

    @Autowired
    MediaEventPublisher mediaEventPublisher;

    @Autowired
    private DynamicTask dynamicTask;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    MediaServerRedisStorage mediaServerRedisStorage;

    private final String mediaKeepaliveKeyPrefix = "media-keepalive-";

    @Override
    public void initMeidiaServer() {
        //获取默认数据库
        MediaServerEntity defaultMediaServer = getDefaultMediaServer();
        MediaServerEntity mediaSerConfig = mediaConfig.getMediaSerConfig();

        if(ObjectUtils.isEmpty(defaultMediaServer)){
            mediaServerRedisStorage.update(mediaSerConfig);
        }else {
            //修改默认节点数据
            BeanUtil.copyProperties(mediaSerConfig,defaultMediaServer);
            mediaServerRedisStorage.update(defaultMediaServer);
        }
        //进行流媒体的连接
        List<MediaServerEntity> allMediaServer = getAllMediaServer();
        for (MediaServerEntity mediaServerEntity : allMediaServer) {
            connectMediaServer(mediaServerEntity);
        }
        //
    }

    @Override
    public MediaServerEntity getDefaultMediaServer(){
        return mediaServerRedisStorage.getDefaultMediaServer();

    }

    public List<MediaServerEntity> getAllMediaServer(){
        return mediaServerRedisStorage.selectAllMediaserver();

    }

    @Async("taskExecutor")
    @Override
    public void connectMediaServer(MediaServerEntity mediaServerEntity){
        //获取连接的配置信息
        MediaServerEntity mediaServerConfigApi = iMediaRestfulApiService.getMediaServerConfigApi(mediaServerEntity);
        if(ObjectUtils.isEmpty(mediaServerConfigApi)){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","数据返回异常为空",mediaServerEntity);
            //下线
            mediaServerOffline(mediaServerEntity);
            return;
        }
        //上线
        mediaServerOnline(mediaServerEntity,mediaServerConfigApi);
    }


    @Override
    public void mediaServerOffline(MediaServerEntity mediaServerEntity) {
        mediaServerEntity.setOnline(0);
        mediaServerRedisStorage.update(mediaServerEntity);
    }

    @Override
    public void mediaServerOnline(MediaServerEntity mediaServerEntity,MediaServerEntity mediaServerConfigApi) {
        mediaServerEntity.setOnline(1);
        String protocol = mediaServerEntity.getEnableHttps() != 0 ? "https" : "http";
        String hookPrex = String.format("%s://%s:%s/index/hook", protocol, mediaServerEntity.getHookIp(), serverPort);
        //进行配置文件设置
        MediaServerConfigDto mediaServerConfigDto = new MediaServerConfigDto();
        BeanUtil.copyProperties(mediaServerConfigApi,mediaServerConfigDto);
        mediaServerConfigDto.setHttpIp(mediaServerEntity.getIp());
        mediaServerConfigDto.setHttpPort(mediaServerEntity.getHttpPort());
        mediaServerConfigDto.setSchedulerIp(mediaServerEntity.getHookIp());
        mediaServerConfigDto.setSchedulerPort(serverPort);
        mediaServerConfigDto.setMediaServerId(mediaServerEntity.getId());
        mediaServerConfigDto.setMsgPushEnable(1);
        mediaServerConfigDto.setRegisterMediaNode(String.format("%s/registerMediaNode", hookPrex));
        mediaServerConfigDto.setUnregisterMediaNode(String.format("%s/unregisterMediaNode", hookPrex));
        mediaServerConfigDto.setStreamNoneReader(String.format("%s/streamNoneReader", hookPrex));
        mediaServerConfigDto.setStreamArrive(String.format("%s/streamArrive", hookPrex));
        mediaServerConfigDto.setStreamArrive(String.format("%s/streamArrive", hookPrex));
        mediaServerConfigDto.setOnStreamDisconnect(String.format("%s/onStreamDisconnect", hookPrex));
        mediaServerConfigDto.setServerKeepalive(String.format("%s/serverKeepalive", hookPrex));
        mediaServerConfigDto.setOnStreamNotFound(String.format("%s/onStreamNotFound", hookPrex));
        mediaServerConfigDto.setOnPublish(String.format("%s/onPublish", hookPrex));
        mediaServerConfigDto.setRtpPortRange(mediaServerEntity.getRtpPortRange());
        Boolean aBoolean = iMediaRestfulApiService.setMediaServerConfigApi(mediaServerConfigDto, mediaServerEntity);
        log.warn(LogTemplate.PROCESS_LOG_TEMPLATE,"流媒体服务设置，设置结果为:",aBoolean);
        mediaServerRedisStorage.update(mediaServerEntity);
        //监听事件进行流媒体上线业务处理
        mediaEventPublisher.meidiaOnlineEventPublish(mediaServerEntity);
    }

    @Override
    public int add(MediaServerEntity mediaServerEntity) {
        return 0;
    }

    @Override
    public void updateMediaServerKeepalive(String mediaServerId) {
        MediaServerEntity mediaServerEntity = getOneMediaServer(mediaServerId);
        if (mediaServerEntity == null) {
            // zlm连接重试
            log.error(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", "心跳保活失败,获取流媒体异常");
            return;
        }
        final String zlmKeepaliveKey = mediaKeepaliveKeyPrefix + mediaServerEntity.getId();
        dynamicTask.stop(zlmKeepaliveKey);
        //三次下线
        dynamicTask.startDelay(zlmKeepaliveKey, new KeepAliveTimeoutRunnable(mediaServerEntity), userSetting.getMediaServerKeepaliveInterval() * 3 * 1000);
    }

    class KeepAliveTimeoutRunnable implements Runnable{

        private MediaServerEntity serverItem;

        public KeepAliveTimeoutRunnable(MediaServerEntity serverItem) {
            this.serverItem = serverItem;
        }

        @Override
        public void run() {
            log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", "[流媒体心跳到期]：" + serverItem.getId());

            // 发起http请求验证zlm是否确实无法连接，如果确实无法连接则发送离线事件，否则不作处理
            MediaServerEntity mediaServerConfigApi = iMediaRestfulApiService.getMediaServerConfigApi(serverItem);
            if (ObjectUtils.isEmpty(mediaServerConfigApi)) {
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "媒体服务器节点管理服务", "心跳到期,验证后仍在线，恢复心跳信息,请检查六每日提是否可以正常发送心跳", serverItem.getId());
                // 添加zlm信息
                updateMediaServerKeepalive(serverItem.getId());
            }else {
                //进行流媒体离线通知
                mediaEventPublisher.meidiaOfflineEventPublish(mediaServerConfigApi);
            }
        }
    }

    @Override
    public MediaServerEntity getOneMediaServer(String mediaServerId) {
        return mediaServerRedisStorage.selectByMediaServerId(mediaServerId);
    }

    @Override
    public void registerMediaNode(MediaServerConfigDto mediaServerConfigDto) {
        //判断流媒体的注册
        MediaServerEntity oneMediaServer = getOneMediaServer(mediaServerConfigDto.getMediaServerId());
        if(ObjectUtils.isEmpty(oneMediaServer)){
            connectMediaServer(oneMediaServer);
        }else {
            //离线变上线
            if(oneMediaServer.getOnline()!=1){
                connectMediaServer(oneMediaServer);
            }
        }
    }

    @Override
    public void unRegisterMediaNode(String mediaServerId) {
        MediaServerEntity mediaServerEntity = getOneMediaServer(mediaServerId);
        if (mediaServerEntity == null) {
            // zlm连接重试
            log.error(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", "心跳保活失败,获取流媒体异常");
            return;
        }
        //进行流媒体下线
        mediaEventPublisher.meidiaOfflineEventPublish(mediaServerEntity);
    }
}
