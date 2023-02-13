package com.runjian.media.dispatcher.mq.event.signIn;


import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.dto.resp.DispatcherSignInRsp;
import com.runjian.media.dispatcher.service.DispatcherInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 网关注册上线订阅
 */
@Component
@Slf4j
public class DispatcherInfoSignInListener implements ApplicationListener<DispatcherInfoSignInEvent> {



    @Autowired
    DispatcherInfoService dispatcherInfoService;


    @Autowired
    DispatcherSignInConf dispatcherSignInConf;




    @Override
    public void onApplicationEvent(DispatcherInfoSignInEvent event) {
        DispatcherSignInRsp gatewaySignInRsp = event.getGatewaySignInRsp();
        BeanUtils.copyProperties(gatewaySignInRsp,dispatcherSignInConf);
        dispatcherInfoService.addMqListener(gatewaySignInRsp.getMqGetQueue());

    }

}
