package com.runjian.mq.event.signIn;


import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.domain.resp.GatewaySignInRsp;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.service.IGatewayInfoService;
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
public class GatewayInfoSignInListener implements ApplicationListener<GatewayInfoSignInEvent> {

    @Autowired
    GatewaySignInConf gatewaySignInConf;

    @Autowired
    IGatewayInfoService gatewayInfoService;

    @Autowired
    IMqMsgDealServer mqMsgDealServer;




    @Override
    public void onApplicationEvent(GatewayInfoSignInEvent event) {
        GatewaySignInRsp gatewaySignInRsp = event.getGatewaySignInRsp();
        //设置全局的bean的值 方便以后获取
        BeanUtils.copyProperties(gatewaySignInRsp,gatewaySignInConf);
        //进行动态业务队列队列创建

        gatewayInfoService.addMqListener(gatewaySignInRsp.getMqGetQueue());

        //进行全量的设备数据数据同步，避免出现设备数据差异
        CommonMqDto commonMqDto = new CommonMqDto();
        commonMqDto.setMsgType(GatewayBusinessMsgType.DEVICE_TOTAL_SYNC.getTypeName());
        mqMsgDealServer.msgProcess(commonMqDto);
    }

}
