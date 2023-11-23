package com.runjian.media.dispatcher.service;

import com.alibaba.fastjson.JSONObject;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenjialing
 */
public interface IOnlineStreamsService {
    /**
     * 更新流在线列表
     * @param onlineStreams
     * @return
     */
    int update(OnlineStreamsEntity onlineStreams);

    int save(OnlineStreamsEntity onlineStreams);

    /**
     * 获取一个流信息
     * @param streamId
     * @return
     */
    OnlineStreamsEntity getOneBystreamId(String streamId);

    OnlineStreamsEntity streamByChannelInfo(String deviceId,String channelId);

    /**
     * 移除流列表
     * @param streamId
     * @return
     */
    int remove(String streamId);

    /**
     * 移除流列表
     * @return
     */
    int removeAll();

    /**
     * 根据流id 移除流列表
     * streamList
     * @return
     */
    int removeByStreamList(List<String> streamIdList);


    /**
     * 获取流媒体中的全部在线播放流列表
     * @param mediaServerId
     * @return
     */
    List<OnlineStreamsEntity > streamList(String mediaServerId);

    /**
     * 通过流id获取流列表数据
     * @param streamLists
     * @return
     */
    List<OnlineStreamsEntity > streamListByStreamIds(List<String> streamLists);

    /**
     * 查找时间之前的流
     * @param streamLists
     * @param checkTime
     * @return
     */
    List<OnlineStreamsEntity > streamListByCheckTime(List<String> streamLists, LocalDateTime checkTime);
    /**
     * 获取流媒体中的全部在线播放流列表
     * @return
     */
    List<OnlineStreamsEntity > streamAll();
}
