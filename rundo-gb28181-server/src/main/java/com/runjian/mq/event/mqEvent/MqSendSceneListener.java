package com.runjian.mq.event.mqEvent;


import com.runjian.common.constant.LogTemplate;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 网关注册上线订阅
 * @author chenjialing
 */
@Component
@Slf4j
public class MqSendSceneListener implements ApplicationListener<MqSendSceneEvent> {



    @Autowired
    GatewayBusinessAsyncSender businessAsyncSender;





    @Override
    public void onApplicationEvent(MqSendSceneEvent event) {
        MqSendSceneDto mqSendScene = event.getMqSendSceneDto();
        try {
            businessAsyncSender.sendforAllScene(mqSendScene.getStreamBusinessSceneResp(),mqSendScene.getBusinessErrorEnums(),mqSendScene.getData());

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","数据执行失败",mqSendScene,e);
        }

    }

}
