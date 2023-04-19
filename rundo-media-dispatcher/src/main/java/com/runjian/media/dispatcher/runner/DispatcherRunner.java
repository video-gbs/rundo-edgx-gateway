package com.runjian.media.dispatcher.runner;

import com.runjian.media.dispatcher.service.DispatcherInfoService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
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
public class DispatcherRunner implements CommandLineRunner {




    @Autowired
    DispatcherInfoService dispatcherInfoService;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Override
    public void run(String... args) throws Exception {
        //初始化国标的ssrc
        redisCatchStorageService.ssrcInit();
        //发送注册
        dispatcherInfoService.sendRegisterInfo();
    }
}
