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
    //调度服务的消息  end
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
    //停播 调度服务请求网关
    STOP_PLAY("CHANNEL_STOP_PLAY"),
    //云台控制
    PTZ_CONTROL("CHANNEL_PTZ_CONTROL"),
    //录像列表
    RECORD_INFO("CHANNEL_RECORD_INFO"),
    //全量设备信息
    DEVICE_TOTAL_SYNC("DEVICE_TOTAL_SYNC"),
    //设备录像倍速
    DEVICE_RECORD_SPEED("DEVICE_RECORD_SPEED"),
    //回放拖动播放
    DEVICE_RECORD_SEEK("DEVICE_RECORD_SEEK"),
    //回放暂停
    DEVICE_RECORD_PAUSE("DEVICE_RECORD_PAUSE"),
    //回放恢复
    DEVICE_RECORD_RESUME("DEVICE_RECORD_RESUME"),
    //预置位--操作查询
    CHANNEL_PTZ_PRESET("CHANNEL_PTZ_PRESET"),
    //云台控制操作
    CHANNEL_PTZ_OPERATION("CHANNEL_PTZ_OPERATION"),
    //拉框放大/缩小
    CHANNEL_3D_OPERATION("CHANNEL_PTZ_3D"),



    /********调度服务相关*************/
    //流注册  调度终于网关均需要处理
    PLAY_STREAM_CALLBACK("PLAY_STREAM_CALLBACK"),
    //流无人观看
    PLAY_NONE_STREAM_READER_CALLBACK("PLAY_NONE_STREAM_READER_CALLBACK"),
    //调度服务绑定
    GATEWAY_BIND_MEDIA("GATEWAY_BIND_MEDIA"),

    /********调度服务业务队列场景*************/
    //推流的结果 推流至gateway网关 isSuccess true或则false
    STREAM_PLAY_RESULT("STREAM_PLAY_RESULT"),
    //无人观看isError=false 和异常断流isError=true
    STREAM_CLOSE("STREAM_CLOSE"),
    //流停止场景
    STREAM_PLAY_STOP("STREAM_PLAY_STOP"),
    //录像播放检查
    STREAM_CHECK_RECORD("STREAM_CHECK_RECORD"),
    //流在线播放检查
    STREAM_CHECK_STREAM("STREAM_CHECK_STREAM"),
    //调度服务全部流停止
    STREAM_STOP_ALL("STREAM_STOP_ALL"),
    //设备录像倍速
    STREAM_RECORD_SPEED("STREAM_RECORD_SPEED"),
    //回放拖动播放
    STREAM_RECORD_SEEK("STREAM_RECORD_SEEK"),
    //回放暂停
    STREAM_RECORD_PAUSE("STREAM_RECORD_PAUSE"),
    //回放恢复
    STREAM_RECORD_RESUME("STREAM_RECORD_RESUME"),
    //获取流信息
    STREAM_MEDIA_INFO("STREAM_MEDIA_INFO"),
    /******调度服务业务队列场景*************/


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