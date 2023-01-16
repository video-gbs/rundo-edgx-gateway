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
    GATEWAY_SIGN_IN("SIGN_IN"),
    //心跳
    HEARTBEAT("HEARTBEAT"),


//    设备相关消息  start
//    设备注册
    REGISTER("DEVICE_SIGN_IN"),
//    设备信息
    DEVICEINFO("DEVICE_SYNC"),
//    设备通道
    CATALOG("CHANNEL_SYNC"),
    //点播
    PLAY("CHANNEL_PLAY"),

    ;

    private final String typeName;

    public static GatewayMsgType getTypeByTypeId(String id){
        id = id.toUpperCase();
        for (GatewayMsgType videoType : values()){
            if (videoType.typeName.equals(id))
                return videoType;
        }
        return null;
    }
}