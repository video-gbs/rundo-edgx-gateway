package com.runjian.media.dispatcher.zlm.service;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayRtpSendReq;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamCheckListResp;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
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
     * @return
     */
    SsrcInfo openRTPServer(MediaServerItem mediaServerItem,  String streamId,String ssrc,Boolean ssrcCheck,Integer port);


    Boolean closeRTPServer(MediaServerItem mediaServerItem, String streamId);

    Boolean closeRTPServer(String mediaServerId, String streamId);



    void update(MediaServerItem mediaSerItem);



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

    /**
     * 组装流地址
     * @param mediaInfo
     * @param app
     * @param stream
     * @return
     */
    StreamInfo getStreamInfoByAppAndStream(MediaServerItem mediaInfo, String app, String stream);

    boolean checkRtpServer(MediaServerItem mediaServerItem,String stream);


    /**
     * rtp转推流的信息
     * @param mediaServerId
     * @param app
     * @param streamId
     * @param gatewayRtpSendReq
     * @return
     */
    SsrcInfo rtpSendServer(String mediaServerId,String app,String streamId, GatewayRtpSendReq gatewayRtpSendReq);

}
