package com.runjian.media.manager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.runjian.media.manager.dto.entity.MediaServerEntity;

/**
 * @author chenjialing
 */
public interface IMediaServerService extends IService<MediaServerEntity> {
    /**
     * 初始化流媒体
     */
    void initMeidiaServer();

    /**
     * 流媒体下线
     * @param mediaServerEntity
     */
    void mediaServerOffline(MediaServerEntity mediaServerEntity);

    MediaServerEntity getDefaultMediaServer();
    /**
     * 在线
     * @param mediaServerEntity
     */
    void mediaServerOnline(MediaServerEntity mediaServerEntity,MediaServerEntity mediaServerConfigApi);

    int add(MediaServerEntity mediaServerEntity);
}
