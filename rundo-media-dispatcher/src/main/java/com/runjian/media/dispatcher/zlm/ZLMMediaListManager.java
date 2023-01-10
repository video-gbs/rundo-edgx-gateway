package com.runjian.media.dispatcher.zlm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主要用于国标级联推拉流的异步操作
 * todo 暂时用不上 具体场景再进行改造
 * @author cjl
 *
 *
 */
@Component
public class ZLMMediaListManager {

//    private Logger logger = LoggerFactory.getLogger("ZLMMediaListManager");
//
//    @Autowired
//    private ZLMRESTfulUtils zlmresTfulUtils;
//
//    @Autowired
//    private IRedisCatchStorage redisCatchStorage;
//
//    @Autowired
//    private IVideoManagerStorage storager;
//
//    @Autowired
//    private GbStreamMapper gbStreamMapper;
//
//    @Autowired
//    private PlatformGbStreamMapper platformGbStreamMapper;
//
//    @Autowired
//    private IStreamPushService streamPushService;
//
//    @Autowired
//    private IStreamProxyService streamProxyService;
//
//    @Autowired
//    private StreamPushMapper streamPushMapper;
//
//    @Autowired
//    private ZlmHttpHookSubscribe subscribe;
//
//    @Autowired
//    private UserSetting userSetting;
//
//    @Autowired
//    private ZLMRTPServerFactory zlmrtpServerFactory;
//
//    @Autowired
//    private IMediaServerService mediaServerService;
//
//    private Map<String, ChannelOnlineEvent> channelOnPublishEvents = new ConcurrentHashMap<>();
//
//    public StreamPushItem addPush(MediaItem mediaItem) {
//        StreamPushItem transform = streamPushService.transform(mediaItem);
//        StreamPushItem pushInDb = streamPushService.getPush(mediaItem.getApp(), mediaItem.getStream());
//        transform.setPushIng(mediaItem.isRegist());
//        transform.setUpdateTime(DateUtil.getNow());
//        transform.setPushTime(DateUtil.getNow());
//        transform.setSelf(userSetting.getServerId().equals(mediaItem.getSeverId()));
//        if (pushInDb == null) {
//            transform.setCreateTime(DateUtil.getNow());
//            streamPushMapper.add(transform);
//        }else {
//            streamPushMapper.update(transform);
//            gbStreamMapper.updateMediaServer(mediaItem.getApp(), mediaItem.getStream(), mediaItem.getMediaServerId());
//        }
//        ChannelOnlineEvent channelOnlineEventLister = getChannelOnlineEventLister(transform.getApp(), transform.getStream());
//        if ( channelOnlineEventLister != null)  {
//            try {
//                channelOnlineEventLister.run(transform.getApp(), transform.getStream(), transform.getServerId());;
//            } catch (ParseException e) {
//                logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "ZLM 集群管理", "addPush方法异常", e);
//            }
//            removedChannelOnlineEventLister(transform.getApp(), transform.getStream());
//        }
//        return transform;
//    }
//
//    public void sendStreamEvent(String app, String stream, String mediaServerId) {
//        MediaServerItem mediaServerItem = mediaServerService.getOne(mediaServerId);
//        // 查看推流状态
//        if (zlmrtpServerFactory.isStreamReady(mediaServerItem, app, stream)) {
//            ChannelOnlineEvent channelOnlineEventLister = getChannelOnlineEventLister(app, stream);
//            if (channelOnlineEventLister != null)  {
//                try {
//                    channelOnlineEventLister.run(app, stream, mediaServerId);
//                } catch (ParseException e) {
//                    logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "ZLM 集群管理", "sendStreamEvent方法异常", e);
//                }
//                removedChannelOnlineEventLister(app, stream);
//            }
//        }
//    }
//
//    public int removeMedia(String app, String streamId) {
//        // 查找是否关联了国标， 关联了不删除， 置为离线
//        GbStream gbStream = gbStreamMapper.selectOne(app, streamId);
//        int result;
//        if (gbStream == null) {
//            result = storager.removeMedia(app, streamId);
//        }else {
//            result =storager.mediaOffline(app, streamId);
//        }
//        return result;
//    }
//
//    public void addChannelOnlineEventLister(String app, String stream, ChannelOnlineEvent callback) {
//        this.channelOnPublishEvents.put(app + "_" + stream, callback);
//    }
//
//    public void removedChannelOnlineEventLister(String app, String stream) {
//        this.channelOnPublishEvents.remove(app + "_" + stream);
//    }
//
//    public ChannelOnlineEvent getChannelOnlineEventLister(String app, String stream) {
//        return this.channelOnPublishEvents.get(app + "_" + stream);
//    }

}
