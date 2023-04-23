package com.runjian.media.manager.service;

import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.entity.MediaServerEntity;

/**
 * @author chenjialing
 */
public interface IMediaRestfulApiService {
    /**
     * 获取流媒体配置信息
     * * @param mediaServerEntity
     * @return
     */
    MediaServerEntity getMediaServerConfigApi(MediaServerEntity mediaServerEntity);

    /**
     * 获取流媒体配置信息
     * * @param mediaServerConfigDto
     * @return
     */
    Boolean setMediaServerConfigApi(MediaServerConfigDto mediaServerConfigDto,MediaServerEntity mediaServerEntity);
}
