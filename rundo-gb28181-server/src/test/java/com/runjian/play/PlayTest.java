package com.runjian.play;

import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.gb28181.bean.Device;
import com.runjian.service.IDeviceService;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class PlayTest {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    IplayService playService;
    @Test
    public void testPlay(){
        PlayReq playReq = new PlayReq();
        playReq.setChannelId("34020000001310000001");
        playReq.setDeviceId("34020000001110000001");
        playReq.setEnableAudio(false);
        playReq.setMsgId("12345678");
        playReq.setSsrcCheck(Boolean.TRUE);
        playReq.setStreamMode("UDP");

        playService.play(playReq);
    }

    @Test
    public void testPlayBack(){

    }

}
