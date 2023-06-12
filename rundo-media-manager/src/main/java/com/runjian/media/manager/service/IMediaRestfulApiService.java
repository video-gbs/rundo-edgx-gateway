package com.runjian.media.manager.service;

import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
import com.runjian.media.manager.dto.req.CreateServerReq;
import com.runjian.media.manager.dto.req.Gb28181ServerReq;
import com.runjian.media.manager.dto.resp.CreateServerPortRsp;
import com.runjian.media.manager.dto.resp.MediaDispatchInfoRsp;
import com.runjian.media.manager.dto.resp.MediaPlayInfoRsp;

import java.util.List;

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

    /**
     * 获取流列表
     * @param app
     * @param mediaServerEntity
     * @param streamId
     * @return
     */
    List<MediaPlayInfoRsp> getMediaList(String app,String streamId,MediaServerEntity mediaServerEntity);




    /**
     * 获取流列表
     * @param app
     * @param mediaServerEntity
     * @param streamId
     * @return
     */
    List<MediaDispatchInfoRsp> getDispatchList(String app, String streamId, MediaServerEntity mediaServerEntity);

    /**
     * 创建sdk端口
     * @param createServerReq
     * @param mediaServerEntity
     * @return
     */
    CreateServerPortRsp openSDKServer(CreateServerReq createServerReq,MediaServerEntity mediaServerEntity);

    /**
     * 关闭sdk端口
     * @param key
     * @param mediaServerEntity
     * @return
     */
    Boolean closeSDKServer(String key,MediaServerEntity mediaServerEntity);

    /**
     * 创建国标端口
     * @param gb28181ServerReq
     * @param mediaServerEntity
     * @return
     */
    CreateServerPortRsp openRtpServer(Gb28181ServerReq gb28181ServerReq, MediaServerEntity mediaServerEntity);

    /**
     * 关闭国标端口
     * @param key
     * @param mediaServerEntity
     * @return
     */
    Boolean closeRtpServer(String key,MediaServerEntity mediaServerEntity);

}
