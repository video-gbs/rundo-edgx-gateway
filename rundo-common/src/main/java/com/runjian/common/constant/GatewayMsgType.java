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


    /********设备通道服务相关*************/
//    设备注册
    REGISTER("DEVICE_SIGN_IN"),
//    设备信息
    DEVICEINFO("DEVICE_SYNC"),
    //设备删除
    DEVICE_DELETE("DEVICE_DELETE"),
//    设备通道
    CATALOG("CHANNEL_SYNC"),
    //点播
    PLAY("CHANNEL_PLAY"),
    //回放
    PLAY_BACK("CHANNEL_PLAYBACK"),
    //停播
    STOP_PLAY("CHANNEL_STOP_PLAY"),
    //云台控制
    PTZ_CONTROL("CHANNEL_PTZ_CONTROL"),
    //录像列表
    RECORD_INFO("CHANNEL_RECORD_INFO"),
    /********调度服务相关*************/
    //流注册
    PLAY_STREAM_CALLBACK("PLAY_STREAM_CALLBACK"),
    //流无人观看
    PLAY_NONE_STREAM_READER_CALLBACK("PLAY_NONE_STREAM_READER_CALLBACK"),


    /******调度服务相关*************/


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