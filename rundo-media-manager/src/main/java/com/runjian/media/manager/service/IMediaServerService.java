package com.runjian.media.manager.service;

import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.entity.MediaServerEntity;

/**
 * @author chenjialing
 */
public interface IMediaServerService {
    /**
     * 初始化流媒体
     */
    void initMeidiaServer();

    /**
     * 流媒体连接上线
     * @param mediaServerEntity
     */
    void connectMediaServer(MediaServerEntity mediaServerEntity);
    /**
     * 流媒体下线
     * @param mediaServerEntity
     */
    void mediaServerOffline(MediaServerEntity mediaServerEntity);

    /**
     * 获取默认流媒体
     * @return
     */
    MediaServerEntity getDefaultMediaServer();
    /**
     * 在线
     * @param mediaServerEntity
     */
    void mediaServerOnline(MediaServerEntity mediaServerEntity,MediaServerEntity mediaServerConfigApi);

    /**
     * 流媒体添加
     * @param mediaServerEntity
     * @return
     */
    int add(MediaServerEntity mediaServerEntity);

    /**
     * 流媒体心跳处理
     * @param mediaServerId
     */
    void updateMediaServerKeepalive(String mediaServerId);


    /**
     * 获取一个流媒体
     * @param mediaServerId
     * @return
     */
    MediaServerEntity getOneMediaServer(String mediaServerId);

    /**
     * 流媒体注册
     * @param mediaServerConfigDto
     */
    void registerMediaNode(MediaServerConfigDto mediaServerConfigDto);

    /**
     * 流媒体注册
     * @param mediaServerId
     */
    void unRegisterMediaNode(String mediaServerId);
}
