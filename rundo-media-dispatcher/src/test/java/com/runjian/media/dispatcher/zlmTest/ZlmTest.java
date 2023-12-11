package com.runjian.media.dispatcher.zlmTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import com.runjian.media.dispatcher.zlm.ZLMRESTfulUtils;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class ZlmTest {

    @Autowired
    private ZLMRESTfulUtils zlmresTfulUtils;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Test
    public void testZlmPassive() throws InterruptedException {

        MediaServerItem mediaServerItem0 = new MediaServerItem();
        mediaServerItem0.setIp("172.20.0.104");
        mediaServerItem0.setHttpPort(80);
        mediaServerItem0.setSecret("035c73f7-bb6b-4889-a715-d9eb2d1925cc");

        Map<String, Object> param = new HashMap<>();
        param.put("port", 0);
        String stream = "test";
        param.put("enable_tcp", 1);
        param.put("stream_id", stream);
        JSONObject json0 = zlmresTfulUtils.openRtpServer(mediaServerItem0, param);

        //进行远程的调用
        MediaServerItem mediaServerItem1 = new MediaServerItem();
        mediaServerItem1.setIp("172.20.0.115");
        mediaServerItem1.setHttpPort(80);
        mediaServerItem1.setSecret("035c73f7-bb6b-4889-a715-d9eb2d1925cc");
        //tcp主动远程
        Map<String, Object> param1 = new HashMap<>();
        param1.put("app", "live");
        param1.put("vhost","__defaultVhost__");
        param1.put("stream", "test123");
        param1.put("ssrc", "123");
        JSONObject json1 = zlmresTfulUtils.startSendRtpPassive(mediaServerItem1, param1);
        log.info("被动模式返回结果={}|",json1);
        Integer localPort = json1.getInteger("local_port");
        //本机的tcp主动
        JSONObject json2 = zlmresTfulUtils.connectRtpServer(mediaServerItem0, "172.20.0.115",localPort,"test");
        log.info("主动连接返回结果={}|",json2);

    }
//
//    @Test
//    public void testTaskTimer() throws InterruptedException {
//        for (int i = 0; i < 10; i++) {
//            timer.newTimeout(new MsgTimerTask("test_"+i,null),delay,TimeUnit.SECONDS);
//
//
//        }
//        log.info("结束前");
//        Thread.sleep(10000);
//
//    }

    //
    @Test
    public void testTaskTimer() throws InterruptedException {
        Boolean test123 = redisCatchStorageService.addBusinessSceneKey("test123", StreamBusinessMsgType.STREAM_LIVE_PLAY_START, "123");

        log.info("结束前");
        Thread.sleep(20000);

    }

//    @Test
//    public void testTaskTimer2() throws InterruptedException {
//        Boolean test123 = redisCatchStorageService.addBusinessSceneKey("test123", StreamBusinessMsgType.STREAM_LIVE_PLAY_START, "123");
//
//        log.info("结束前");
//        Thread.sleep(10000);
//
//    }

}
