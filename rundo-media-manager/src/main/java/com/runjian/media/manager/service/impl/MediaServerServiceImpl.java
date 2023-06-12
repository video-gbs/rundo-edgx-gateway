package com.runjian.media.manager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.constant.LogTemplate;
import com.runjian.media.manager.conf.MediaConfig;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
import com.runjian.media.manager.mapper.MediaServerMapper;
import com.runjian.media.manager.service.IMediaRestfulApiService;
import com.runjian.media.manager.service.IMediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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



        mediaServerMapper.updateById(mediaServerEntity);

        //设置流媒体的信息

    }

    @Override
    public int add(MediaServerEntity mediaServerEntity) {
        return 0;
    }
}
