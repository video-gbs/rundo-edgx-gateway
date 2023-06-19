package com.runjian.media.manager.event.mqEvent;


import com.runjian.common.constant.LogTemplate;
import com.runjian.media.manager.conf.mq.DispatcherSignInConf;
import com.runjian.media.manager.dto.resp.DispatcherSignInRsp;
import com.runjian.media.manager.mq.dispatcherBusiness.asyncSender.BusinessAsyncSender;
import com.runjian.media.manager.service.DispatcherInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
    BusinessAsyncSender businessAsyncSender;





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
