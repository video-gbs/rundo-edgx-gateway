package com.runjian.media.dispatcher.service;

import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;

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
     * 流注册相关的操作
     * @param streamId
     */
    void streamChangeDeal(String streamId,Boolean regist);

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
     * 获取流媒体中的全部在线播放流列表
     * @return
     */
    List<OnlineStreamsEntity > streamAll();
}
