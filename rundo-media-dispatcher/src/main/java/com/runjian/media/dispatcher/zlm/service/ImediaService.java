package com.runjian.media.dispatcher.zlm.service;


import com.runjian.common.commonDto.StreamInfo;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;

/**
 * 媒体信息业务
 * @author chenjialing
 */
public interface ImediaService {

    /**
     * 根据应用名和流ID获取播放地址, 通过zlm接口检查是否存在
     * @param app
     * @param stream
     * @return
     */
    StreamInfo getStreamInfoByAppAndStreamWithCheck(String app, String stream, String mediaServerId, String addr, boolean authority);


    /**
     * 根据应用名和流ID获取播放地址, 通过zlm接口检查是否存在, 返回的ip使用远程访问ip，适用与zlm与wvp在一台主机的情况
     * @param app
     * @param stream
     * @return
     */
    StreamInfo getStreamInfoByAppAndStreamWithCheck(String app, String stream, String mediaServerId, boolean authority);

    /**
     * 根据应用名和流ID获取播放地址, 只是地址拼接
     * @param app
     * @param stream
     * @return
     */
    StreamInfo getStreamInfoByAppAndStream(MediaServerItem mediaServerItem, String app, String stream, Object tracks, String callId);

    /**
     * 根据应用名和流ID获取播放地址, 只是地址拼接，返回的ip使用远程访问ip，适用与zlm与wvp在一台主机的情况
     * @param app
     * @param stream
     * @return
     */
    StreamInfo getStreamInfoByAppAndStream(MediaServerItem mediaInfo, String app, String stream, Object tracks, String addr, String callId);
}
