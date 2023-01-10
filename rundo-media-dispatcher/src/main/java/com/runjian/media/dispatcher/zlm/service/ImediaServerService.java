package com.runjian.media.dispatcher.zlm.service;

import com.alibaba.fastjson.JSONObject;
import com.runjian.media.dispatcher.zlm.ZLMServerConfig;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.dto.SSRCInfo;

import java.util.List;

/**
 * 媒体服务节点
 * @author chenjialing
 */
public interface ImediaServerService {

    List<MediaServerItem> getAllMediaServer();

    List<MediaServerItem> getAll();

    List<MediaServerItem> getAllFromDatabase();

    List<MediaServerItem> getAllOnline();

    MediaServerItem getOne(String generalMediaServerId);

    void syncCatchFromDatabase();

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

    MediaServerItem getMediaServerForMinimumLoad();

    void setZLMConfig(MediaServerItem mediaServerItem, boolean restart);

    void updateVmServer(List<MediaServerItem>  mediaServerItemList);

    SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, boolean ssrcCheck, String ssrc,int port);


    void closeRTPServer(MediaServerItem mediaServerItem, String streamId);

    void closeRTPServer(String mediaServerId, String streamId);

    void clearRTPServer(MediaServerItem mediaServerItem);

    void update(MediaServerItem mediaSerItem);

    void addCount(String mediaServerId);

    void removeCount(String mediaServerId);

    void releaseSsrc(String mediaServerItemId, String ssrc);

    void clearMediaServerForOnline();

    void add(MediaServerItem mediaSerItem);

    int addToDatabase(MediaServerItem mediaSerItem);

    int updateToDatabase(MediaServerItem mediaSerItem);

    void resetOnlineServerItem(MediaServerItem serverItem);

    MediaServerItem checkMediaServer(String ip, int port, String secret);

    boolean checkMediaRecordServer(String ip, int port);

    void delete(String id);

    void deleteDb(String id);

    MediaServerItem getDefaultMediaServer();

    MediaServerItem getDefaultMediaServer(String id);

    void updateMediaServerKeepalive(String mediaServerId, JSONObject data);

    void removeMediaServer(String id);

    boolean checkRtpServer(MediaServerItem mediaServerItem, String rtp, String stream);
}
