package com.runjian.mq.event.signIn;


import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.domain.resp.GatewaySignInRsp;
import com.runjian.service.IGatewayInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 网关注册上线订阅
 */
@Component
@Slf4j
public class GatewayInfoSignInListener implements ApplicationListener<GatewayInfoSignInEvent> {

    @Autowired
    GatewaySignInConf gatewaySignInConf;

    @Autowired
    IGatewayInfoService gatewayInfoService;






    @Override
    public void onApplicationEvent(GatewayInfoSignInEvent event) {
        GatewaySignInRsp gatewaySignInRsp = event.getGatewaySignInRsp();
        //设置全局的bean的值 方便以后获取
        BeanUtils.copyProperties(gatewaySignInRsp,gatewaySignInConf);
        //进行动态业务队列队列创建

        gatewayInfoService.addMqListener(gatewaySignInRsp.getMqGetQueue());

    }

}
