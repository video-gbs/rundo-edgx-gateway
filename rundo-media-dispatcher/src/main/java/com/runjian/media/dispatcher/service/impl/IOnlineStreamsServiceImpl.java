package com.runjian.media.dispatcher.service.impl;

import com.runjian.common.mq.RabbitMqSender;
import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.mapper.OnlineStreamsMapper;
import com.runjian.media.dispatcher.service.IOnlineStreamsService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import com.runjian.media.dispatcher.zlm.ZlmHttpHookSubscribe;
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
    @Autowired
    OnlineStreamsMapper onlineStreamsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    DispatcherSignInConf dispatcherSignInConf;

    @Autowired
    RabbitMqSender rabbitMqSender;


    @Autowired
    private ZlmHttpHookSubscribe subscribe;


    @Override
    public int update(OnlineStreamsEntity onlineStreams) {
        return onlineStreamsMapper.update(onlineStreams);

    }

    @Override
    public int save(OnlineStreamsEntity onlineStreams) {
        return onlineStreamsMapper.add(onlineStreams);
    }

    @Override
    public OnlineStreamsEntity getOneBystreamId(String streamId) {
        return onlineStreamsMapper.selectOne(streamId);

    }

    @Override
    public OnlineStreamsEntity streamByChannelInfo(String deviceId, String channelId) {
        return onlineStreamsMapper.selectOneBychannelId(deviceId, channelId);
    }

    @Override
    public int remove(String streamId) {
        return onlineStreamsMapper.deleteBystreamId(streamId);
    }

    @Override
    public int removeAll() {
        return onlineStreamsMapper.deleteAll();
    }

    @Override
    public int removeByStreamList(List<String> streamIdList) {
        return onlineStreamsMapper.deleteBystreamIdList(streamIdList);
    }



    @Override
    public List<OnlineStreamsEntity> streamList(String mediaServerId) {


        return onlineStreamsMapper.selectStreamsByMediaServerId(mediaServerId);
    }

    @Override
    public List<OnlineStreamsEntity> streamListByStreamIds(List<String> streamLists) {
        //获取数据库中的数据
        return onlineStreamsMapper.selectStreamsByStreamIds(streamLists);
    }

    @Override
    public List<OnlineStreamsEntity> streamListByCheckTime(List<String> streamLists, LocalDateTime checkTime) {
        return onlineStreamsMapper.selectStreamsByCheckTime(streamLists,checkTime);
    }

    @Override
    public List<OnlineStreamsEntity> streamAll() {
        return onlineStreamsMapper.selectAll();
    }
}
