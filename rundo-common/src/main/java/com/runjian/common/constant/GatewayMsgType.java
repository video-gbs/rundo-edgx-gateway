package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 网关的消息场景类型
 * @author cjl
 * @date 2022/5/23 17:00
 */
@Getter
@ToString
@AllArgsConstructor
public enum GatewayMsgType {
//    网关自身的消息  start
    //注册
    GATEWAY_SIGN_IN("GATEWAY_SIGN_IN"),
    //心跳
    GATEWAY_HEARTBEAT("GATEWAY_HEARTBEAT"),
    //重新注册
    GATEWAY_RE_SIGN_IN("GATEWAY_RE_SIGN_IN"),
//    网关自身的消息  end

    //调度服务的消息  start
    //注册
    DISPATCH_SIGN_IN  ("DISPATCH_SIGN_IN"),
    //心跳
    DISPATCH_HEARTBEAT("DISPATCH_HEARTBEAT"),
    //重新注册
    DISPATCH_RE_SIGN_IN("DISPATCH_RE_SIGN_IN"),

    ;
    //调度服务的消息  end
    /********设备通道服务相关*************/

    private final String typeName;

}