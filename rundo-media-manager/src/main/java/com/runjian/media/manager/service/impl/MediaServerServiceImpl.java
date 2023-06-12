package com.runjian.media.manager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.constant.LogTemplate;
import com.runjian.media.manager.conf.MediaConfig;
import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
import com.runjian.media.manager.mapper.MediaServerMapper;
import com.runjian.media.manager.service.IMediaRestfulApiService;
import com.runjian.media.manager.service.IMediaServerService;
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
public class MediaServerServiceImpl extends ServiceImpl<MediaServerMapper, MediaServerEntity> implements IMediaServerService {

    @Autowired
    MediaConfig mediaConfig;

    @Autowired
    MediaServerMapper mediaServerMapper;

    @Autowired
    IMediaRestfulApiService iMediaRestfulApiService;


    @Value("${server.port}")
    private Integer serverPort;

    @Override
    public void initMeidiaServer() {
        //获取默认数据库
        MediaServerEntity defaultMediaServer = getDefaultMediaServer();
        MediaServerEntity mediaSerConfig = mediaConfig.getMediaSerConfig();

        if(ObjectUtils.isEmpty(defaultMediaServer)){
            mediaServerMapper.insert(mediaSerConfig);
        }else {
            //修改默认节点数据
            BeanUtil.copyProperties(mediaSerConfig,defaultMediaServer);
            LambdaQueryWrapper<MediaServerEntity> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(MediaServerEntity::isDefaultServer,1);
            mediaServerMapper.update(defaultMediaServer,updateWrapper);
        }
        //进行流媒体的连接
        List<MediaServerEntity> allMediaServer = getAllMediaServer();
        for (MediaServerEntity mediaServerEntity : allMediaServer) {
            connectZlmServer(mediaServerEntity);
        }
        //
    }

    @Override
    public MediaServerEntity getDefaultMediaServer(){
        LambdaQueryWrapper<MediaServerEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MediaServerEntity::isDefaultServer,1);
        queryWrapper.last("limit 1");
        return mediaServerMapper.selectOne(queryWrapper);

    }

    public List<MediaServerEntity> getAllMediaServer(){
        return mediaServerMapper.selectList(null);

    }

    @Async("taskExecutor")
    public void connectZlmServer(MediaServerEntity mediaServerEntity){
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

        mediaServerMapper.updateById(mediaServerEntity);
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
        log.warn(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务设置","设置结果为:",aBoolean);
        mediaServerMapper.updateById(mediaServerEntity);
    }

    @Override
    public int add(MediaServerEntity mediaServerEntity) {
        return 0;
    }
}
