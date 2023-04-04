package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 网关类型枚举
 * @author cjl
 * @date 2022/5/23 17:00
 */
@Getter
@ToString
@AllArgsConstructor
public enum PtzOperationTypeEnum {
    //    国标协议
    //云镜控制
    Left("Left"),
    Right("Right"),
    Up("Up"),
    Down("Down"),
    Upleft("Upleft"),
    Upright("Upright"),
    Downleft("Downleft"),
    Downright("Downright"),
    //倍率缩小
    Zoomin("Zoomin"),
    //倍率放大
    Zoomout("Zoomout"),

    PtzStop("PtzStop"),
    //预置位
    PresetGet("PresetGet"),
    PresetSet("PresetSet"),
    PresetInvoke("PresetInvoke"),
    PresetDel("PresetDel"),

    //F1 指令
    //光圈缩小放大
    IrisReduce("IrisReduce"),
    IrisGrow("IrisGrow"),
    //聚焦近远
    FocusNear("FocusNear"),
    FocusFar("FocusFar"),
    //F1停止stop
    IrisAndFocusStop("IrisAndFocusStop"),


    ;

    private final String typeName;

    public static PtzOperationTypeEnum getTypeByTypeId(String id){
        id = id.toUpperCase();
        for (PtzOperationTypeEnum videoType : values()){
            if (videoType.typeName.equals(id))
                return videoType;
        }
        return null;
    }
}