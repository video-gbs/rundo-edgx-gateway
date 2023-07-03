package com.runjian.media.manager.controller;

import cn.hutool.core.bean.BeanUtil;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.media.manager.conf.MediaConfig;
import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
import com.runjian.media.manager.service.IMediaRestfulApiService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class MediaRestfulApiController {
    @Autowired
    MediaConfig mediaConfig;

    @Autowired
    IMediaRestfulApiService iMediaRestfulApiService;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void getMediaServerConfigApiTest(){
        MediaServerEntity mediaSerConfig = mediaConfig.getMediaSerConfig();
        iMediaRestfulApiService.getMediaServerConfigApi(mediaSerConfig);
    }

    @Test
    public void setMediaServerConfigApiTest(){
        MediaServerEntity mediaSerConfig = mediaConfig.getMediaSerConfig();
        MediaServerConfigDto mediaServerConfigDto = new MediaServerConfigDto();
        BeanUtil.copyProperties(mediaSerConfig,mediaServerConfigDto);
        mediaServerConfigDto.setHttpIp(mediaSerConfig.getIp());
        mediaServerConfigDto.setStreamNoneReaderDelayMS(30);
        iMediaRestfulApiService.setMediaServerConfigApi(mediaServerConfigDto,mediaSerConfig);
    }

    @Test
    public void testRedissonQueueAdd(){
        RBlockingDeque<Object> va = redissonClient.getBlockingDeque("testQueue");
        StreamBusinessSceneResp<Object> objectStreamBusinessSceneResp = StreamBusinessSceneResp.addSceneReady(StreamBusinessMsgType.STREAM_CHECK_RECORD, "0", "test",null);


        va.add(objectStreamBusinessSceneResp);

    }

    @Test
    public void testRedissonQueueRemove(){
        RBlockingDeque<Object> va = redissonClient.getBlockingDeque("testQueue");
        StreamBusinessSceneResp<Object> objectStreamBusinessSceneResp = StreamBusinessSceneResp.addSceneReady(StreamBusinessMsgType.STREAM_CHECK_RECORD, "0", "test",null);


        va.remove(objectStreamBusinessSceneResp);

    }
}
