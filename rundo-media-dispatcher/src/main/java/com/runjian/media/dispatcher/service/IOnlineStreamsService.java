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
}
