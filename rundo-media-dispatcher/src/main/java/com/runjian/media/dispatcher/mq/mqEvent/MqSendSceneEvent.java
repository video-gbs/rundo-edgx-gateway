package com.runjian.media.dispatcher.mq.mqEvent;

import com.runjian.common.config.response.StreamBusinessSceneResp;
import org.springframework.context.ApplicationEvent;

/**
 * 网关注册消息通知
 * @author chenjialing
 */
public class MqSendSceneEvent extends ApplicationEvent {
    public MqSendSceneEvent(Object source) {
        super(source);
    }

    private StreamBusinessSceneResp mqSendSceneDto;

    public StreamBusinessSceneResp getMqSendSceneDto() {
        return mqSendSceneDto;
    }

    public void setMqSendSceneDto(StreamBusinessSceneResp mqSendSceneDto) {
        this.mqSendSceneDto = mqSendSceneDto;
    }
}
