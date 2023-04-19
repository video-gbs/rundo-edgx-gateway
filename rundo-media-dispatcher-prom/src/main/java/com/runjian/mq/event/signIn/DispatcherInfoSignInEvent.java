package com.runjian.mq.event.signIn;

import com.runjian.media.dispatcher.dto.resp.DispatcherSignInRsp;
import org.springframework.context.ApplicationEvent;

/**
 * 网关注册消息通知
 * @author chenjialing
 */
public class DispatcherInfoSignInEvent extends ApplicationEvent {
    public DispatcherInfoSignInEvent(Object source) {
        super(source);
    }

    private DispatcherSignInRsp gatewaySignInRsp;

    public DispatcherSignInRsp getGatewaySignInRsp() {
        return gatewaySignInRsp;
    }

    public void setGatewaySignInRsp(DispatcherSignInRsp gatewaySignInRsp) {
        this.gatewaySignInRsp = gatewaySignInRsp;
    }
}
