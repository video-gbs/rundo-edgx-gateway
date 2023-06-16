package com.runjian.media.manager.service.impl;

import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.StreamCloseDto;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.StreamBusinessSceneResp;
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
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class IOnlineStreamsServiceImpl implements IOnlineStreamsService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Boolean update(OnlineStreamsEntity onlineStreams) {
        return RedisCommonUtil.set(redisTemplate, VideoManagerConstants.SELF_STREAMS_ONLINE+onlineStreams.getStreamId(),onlineStreams);

    }

    @Override
    public Boolean save(OnlineStreamsEntity onlineStreams) {
        return RedisCommonUtil.set(redisTemplate, VideoManagerConstants.SELF_STREAMS_ONLINE+onlineStreams.getStreamId(),onlineStreams);
    }

    @Override
    public OnlineStreamsEntity getOneBystreamId(String streamId) {
        return (OnlineStreamsEntity)RedisCommonUtil.get(redisTemplate,VideoManagerConstants.SELF_STREAMS_ONLINE+streamId);

    }

    @Override
    public Boolean remove(String streamId) {

        return RedisCommonUtil.del(redisTemplate,VideoManagerConstants.SELF_STREAMS_ONLINE+streamId);
    }

    @Override
    public int removeAll() {
        Set<String> keys = RedisCommonUtil.keys(redisTemplate, VideoManagerConstants.SELF_STREAMS_ONLINE + "*");
        for(String bKey : keys){
            RedisCommonUtil.del(redisTemplate,bKey);
        }
        return keys.size();
    }

    @Override
    public int removeByStreamList(List<String> streamIdList) {
        streamIdList.forEach(streamId->{
            RedisCommonUtil.del(redisTemplate,VideoManagerConstants.SELF_STREAMS_ONLINE+streamId);
        });
        return streamIdList.size();
    }



    @Override
    public List<OnlineStreamsEntity> streamList(String mediaServerId) {
        Set<String> keys = RedisCommonUtil.keys(redisTemplate, VideoManagerConstants.SELF_STREAMS_ONLINE + "*");
        ArrayList<OnlineStreamsEntity> onlineStreamsEntities = new ArrayList<>();

        for(String bKey : keys){
            OnlineStreamsEntity onlineStreamsEntity = (OnlineStreamsEntity)RedisCommonUtil.get(redisTemplate, bKey);
            if(!ObjectUtils.isEmpty(onlineStreamsEntity)){
                if(onlineStreamsEntity.getMediaServerId().equals(mediaServerId)){
                    onlineStreamsEntities.add(onlineStreamsEntity);
                }
            }

        }


        return onlineStreamsEntities;
    }

    @Override
    public List<OnlineStreamsEntity> streamListByStreamIds(List<String> streamLists) {
        ArrayList<OnlineStreamsEntity> onlineStreamsEntities = new ArrayList<>();
        streamLists.forEach(streamId->{
            OnlineStreamsEntity onlineStreamsEntity = (OnlineStreamsEntity)RedisCommonUtil.get(redisTemplate, VideoManagerConstants.SELF_STREAMS_ONLINE+streamId);
            if(!ObjectUtils.isEmpty(onlineStreamsEntity)){
                onlineStreamsEntities.add(onlineStreamsEntity);
            }
        });
        return onlineStreamsEntities;
    }

    @Override
    public List<OnlineStreamsEntity> streamListByCheckTime(List<String> streamLists, LocalDateTime checkTime) {
        Set<String> keys = RedisCommonUtil.keys(redisTemplate, VideoManagerConstants.SELF_STREAMS_ONLINE + "*");
        ArrayList<OnlineStreamsEntity> onlineStreamsEntities = new ArrayList<>();

        for(String bKey : keys){
            OnlineStreamsEntity onlineStreamsEntity = (OnlineStreamsEntity)RedisCommonUtil.get(redisTemplate, bKey);
            if(!ObjectUtils.isEmpty(onlineStreamsEntity)){
                if(onlineStreamsEntity.getStatus()!= 0){
                    onlineStreamsEntities.add(onlineStreamsEntity);
                }
            }

        }
        return onlineStreamsEntities;
    }

    @Override
    public List<OnlineStreamsEntity> streamAll() {
        Set<String> keys = RedisCommonUtil.keys(redisTemplate, VideoManagerConstants.SELF_STREAMS_ONLINE + "*");
        ArrayList<OnlineStreamsEntity> onlineStreamsEntities = new ArrayList<>();

        for(String bKey : keys){
            OnlineStreamsEntity onlineStreamsEntity = (OnlineStreamsEntity)RedisCommonUtil.get(redisTemplate, bKey);
            if(!ObjectUtils.isEmpty(onlineStreamsEntity)){
                onlineStreamsEntities.add(onlineStreamsEntity);
            }

        }


        return onlineStreamsEntities;
    }
}
