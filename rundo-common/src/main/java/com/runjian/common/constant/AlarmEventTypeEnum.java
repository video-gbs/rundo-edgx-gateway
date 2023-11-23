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
public enum AlarmEventTypeEnum {
//    网关自身的消息  start
// 聚合信息：开始
    COMPOUND_START(1),
    // 聚合信息：心跳
    COMPOUND_HEARTBEAT(2),
    // 聚合信息：结束
    COMPOUND_END(3)

    ;
    //调度服务的消息  end
    /********设备通道服务相关*************/

    private final Integer code;

}