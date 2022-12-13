package com.runjian.gb28181.event.device;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


/**
 * @author lin
 */
@Component
@Slf4j
public class RequestTimeoutEventImpl implements ApplicationListener<RequestTimeoutEvent> {



    @Override
    public void onApplicationEvent(RequestTimeoutEvent event) {
        //设备请求接口板离线事件通知

    }
}
