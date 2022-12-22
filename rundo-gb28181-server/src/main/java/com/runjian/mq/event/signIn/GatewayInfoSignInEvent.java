package com.runjian.mq.event.signIn;

import com.runjian.domain.resp.GatewaySignInRsp;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * 网关注册消息通知
 * @author chenjialing
 */
public class GatewayInfoSignInEvent extends ApplicationEvent {
    public GatewayInfoSignInEvent(Object source) {
        super(source);
    }

    private GatewaySignInRsp gatewaySignInRsp;

    public GatewaySignInRsp getGatewaySignInRsp() {
        return gatewaySignInRsp;
    }

    public void setGatewaySignInRsp(GatewaySignInRsp gatewaySignInRsp) {
        this.gatewaySignInRsp = gatewaySignInRsp;
    }
}
