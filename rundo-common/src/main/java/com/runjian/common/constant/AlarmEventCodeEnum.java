package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 告警事件
 * @author cjl
 * @date 2022/5/23 17:00
 */
@Getter
@ToString
@AllArgsConstructor
public enum AlarmEventCodeEnum {
    //移动侦测
    MOVE_ALARM("1"),
    //遮挡告警
    COVER_ALARM("2"),
    //区域入侵
    REGIONAL_ALARM("3"),
    //绊线入侵
    TRIPPING_WIRE_ALARM("4")

    ;
    //调度服务的消息  end
    /********设备通道服务相关*************/

    private final String code;

}