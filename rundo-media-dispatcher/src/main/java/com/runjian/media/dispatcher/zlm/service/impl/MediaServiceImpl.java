package com.runjian.media.dispatcher.zlm.service.impl;

import com.runjian.common.commonDto.StreamInfo;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.service.ImediaService;
import org.springframework.stereotype.Service;

/**
 * @author chenjialing
 */
@Service
public class MediaServiceImpl implements ImediaService {
    @Override
    public StreamInfo getStreamInfoByAppAndStreamWithCheck(String app, String stream, String mediaServerId, String addr, boolean authority) {
        return null;
    }

    @Override
    public StreamInfo getStreamInfoByAppAndStreamWithCheck(String app, String stream, String mediaServerId, boolean authority) {
        return null;
    }

    @Override
    public StreamInfo getStreamInfoByAppAndStream(MediaServerItem mediaServerItem, String app, String stream, Object tracks, String callId) {
        return null;
    }

    @Override
    public StreamInfo getStreamInfoByAppAndStream(MediaServerItem mediaInfo, String app, String stream, Object tracks, String addr, String callId) {
        return null;
    }
}
