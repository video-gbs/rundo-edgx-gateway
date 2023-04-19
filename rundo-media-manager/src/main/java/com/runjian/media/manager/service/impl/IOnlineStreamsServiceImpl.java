package com.runjian.media.manager.service.impl;

import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.StreamCloseDto;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.manager.dto.entity.OnlineStreamsEntity;
import com.runjian.media.manager.service.IOnlineStreamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class IOnlineStreamsServiceImpl implements IOnlineStreamsService {
    @Override
    public int update(OnlineStreamsEntity onlineStreams) {
        return 0;
    }

    @Override
    public OnlineStreamsEntity getOneBystreamId(String streamId) {
        return null;
    }

    @Override
    public int remove(String streamId) {
        return 0;
    }

    @Override
    public int removeAll() {
        return 0;
    }

    @Override
    public int removeByStreamList(List<String> streamIdList) {
        return 0;
    }

    @Override
    public void streamChangeDeal(String streamId, Boolean regist, String app) {

    }

    @Override
    public List<OnlineStreamsEntity> streamList(String mediaServerId) {
        return null;
    }

    @Override
    public List<OnlineStreamsEntity> streamListByStreamIds(List<String> streamLists) {
        return null;
    }

    @Override
    public List<OnlineStreamsEntity> streamListByCheckTime(List<String> streamLists, LocalDateTime checkTime) {
        return null;
    }

    @Override
    public List<OnlineStreamsEntity> streamAll() {
        return null;
    }
}
