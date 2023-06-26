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
public enum GatewayBusinessMsgType {

    /********设备通道服务相关*************/
//    设备注册
    REGISTER("DEVICE_SIGN_IN"),
//    设备信息
    DEVICEINFO("DEVICE_SYNC"),
    DEVICE_ADD("DEVICE_ADD"),
    //设备删除     //设备软删除
    DEVICE_DELETE("DEVICE_DELETE"),
    DEVICE_DELETE_SOFT("DEVICE_DELETE_SOFT"),
    //通道软硬删除
    CHANNEL_DELETE_HARD("CHANNEL_DELETE_HARD"),
    CHANNEL_DELETE_SOFT("CHANNEL_DELETE_SOFT"),
//    设备通道
    CATALOG("CHANNEL_SYNC"),

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


    /******调度服务调用网关业务队列场景*************/
    //点播
    PLAY("CHANNEL_PLAY"),
    //回放
    PLAY_BACK("CHANNEL_PLAYBACK"),
    //停播 调度服务请求网关
    STOP_PLAY("CHANNEL_STOP_PLAY"),

    /******调度服务调用网关业务队列场景*************/
    ;

    private final String typeName;

    public static GatewayBusinessMsgType getTypeName(String typeName){
        typeName = typeName.toUpperCase();
        for (GatewayBusinessMsgType value : values()){
            if (value.typeName.equals(typeName)){
                return value;
            }
        }
        return null;
    }
}