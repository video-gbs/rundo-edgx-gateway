package com.runjian.media.manager.runner;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.media.manager.service.DispatcherInfoService;
import com.runjian.media.manager.service.IMediaServerService;
import com.runjian.media.manager.service.IRedisCatchStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 服务启动,生成网关配置信息
 * @author chenjialing
 */
@Component
@Order(value = 0)
public class MediaManagerRunner implements CommandLineRunner {




    @Autowired
    DispatcherInfoService dispatcherInfoService;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;



    @Autowired
    IMediaServerService mediaServerService;
    @Override
    public void run(String... args) throws Exception {
        //初始化国标的ssrc
        redisCatchStorageService.ssrcInit();
        //发送注册
        dispatcherInfoService.sendRegisterInfo();
        //进行流媒体注册
        mediaServerService.initMeidiaServer();

        Map<String, String> map1 = new HashMap<>();
        map1.put("orderId", "100");
        map1.put("remark", "其他信息");

        Map<String, String> map2 = new HashMap<>();
        map2.put("orderId", "200");
        map2.put("remark", "其他信息");


        Boolean test = redisCatchStorageService.addBusinessSceneKey(StreamBusinessMsgType.STREAM_MEDIA_INFO.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+ "test", StreamBusinessMsgType.STREAM_MEDIA_INFO, "1");
//        redisCatchStorageService.editBusinessSceneKey("test", BusinessErrorEnums.SUCCESS,null);

    }
}
