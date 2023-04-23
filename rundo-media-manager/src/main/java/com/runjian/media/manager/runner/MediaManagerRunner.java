package com.runjian.media.manager.runner;

import com.runjian.media.manager.service.DispatcherInfoService;
import com.runjian.media.manager.service.IMediaServerService;
import com.runjian.media.manager.service.IRedisCatchStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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
    }
}
