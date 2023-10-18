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
public enum StreamBusinessMsgType {

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
    //直播迁移
    STREAM_LIVE_PLAY_START("STREAM_LIVE_PLAY_START"),
    //webrtc对讲
    STREAM_WEBRTC_TALK("STREAM_WEBRTC_TALK"),
    //录播迁移
    STREAM_RECORD_PLAY_START("STREAM_RECORD_PLAY_START"),
    //自定义直播
    STREAM_CUSTOM_LIVE_START("STREAM_CUSTOM_LIVE_START"),
    //rtc点播
    STREAM_WEBRTC_LIVE_START("STREAM_WEBRTC_LIVE_START"),

    /******调度服务业务队列场景*************/
    ;

    private final String typeName;

    public static StreamBusinessMsgType getTypeName(String typeName){
        typeName = typeName.toUpperCase();
        for (StreamBusinessMsgType value : values()){
            if (value.typeName.equals(typeName)){
                return value;
            }
        }
        return null;
    }
}