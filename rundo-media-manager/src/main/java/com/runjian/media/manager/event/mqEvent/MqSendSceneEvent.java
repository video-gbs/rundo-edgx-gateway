package com.runjian.media.manager.event.mqEvent;

import com.runjian.media.manager.dto.resp.DispatcherSignInRsp;
import org.springframework.context.ApplicationEvent;

/**
 * 网关注册消息通知
 * @author chenjialing
 */
public class MqSendSceneEvent extends ApplicationEvent {
    public MqSendSceneEvent(Object source) {
        super(source);
    }

    private MqSendSceneDto mqSendSceneDto;

    public MqSendSceneDto getMqSendSceneDto() {
        return mqSendSceneDto;
    }

    public void setMqSendSceneDto(MqSendSceneDto mqSendSceneDto) {
        this.mqSendSceneDto = mqSendSceneDto;
    }
}
