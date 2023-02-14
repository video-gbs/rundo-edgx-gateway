package com.runjian.media.dispatcher.zlm.service;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.media.dispatcher.zlm.ZLMServerConfig;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import java.util.List;

/**
 * 媒体服务节点
 * @author chenjialing
 */
public interface ImediaServerService {

    List<MediaServerItem> getAllMediaServer();


    List<MediaServerItem> getAllFromDatabase();


    MediaServerItem getOne(String generalMediaServerId);


    /**
     * 新的节点加入
     * @param zlmServerConfig
     * @return
     */
    void zlmServerOnline(ZLMServerConfig zlmServerConfig);

    /**
     * 节点离线
     * @param mediaServerId
     * @return
     */
    void zlmServerOffline(String mediaServerId);


    void setZLMConfig(MediaServerItem mediaServerItem, boolean restart);

    /**
     * 创建端口
     * @param mediaServerItem
     * @param baseRtpServerDto
     * @return
     */
    SsrcInfo openRTPServer(MediaServerItem mediaServerItem, BaseRtpServerDto baseRtpServerDto);


    Boolean closeRTPServer(MediaServerItem mediaServerItem, String streamId);

    Boolean closeRTPServer(String mediaServerId, String streamId);



    void update(MediaServerItem mediaSerItem);

    /**
     *
     * @param mediaServerId
     * @param streamId
     * @param app
     * @return
     */
    StreamInfo getRtpInfo(String mediaServerId, String streamId,String app);



    void add(MediaServerItem mediaSerItem);

    int addToDatabase(MediaServerItem mediaSerItem);

    int updateToDatabase(MediaServerItem mediaSerItem);


    MediaServerItem checkMediaServer(String ip, int port, String secret);

    boolean checkMediaRecordServer(String ip, int port);


    void deleteDb(String id);

    MediaServerItem getDefaultMediaServer();

    MediaServerItem getDefaultMediaServer(String id);

    void updateMediaServerKeepalive(String mediaServerId, JSONObject data);

    void removeMediaServer(String id);

    boolean checkRtpServer(MediaServerItem mediaServerItem, String rtp, String stream);

    /**
     * 停止流
     * @param streamId
     * @param msgId
     */
    void streamBye(String streamId,String msgId);

}
