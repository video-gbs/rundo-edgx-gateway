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

    /**预置位
     * start
     */

//    /**预置位
//     * end
//     */
//    public static final int PTZ_LEFT = 2;
//    public static final int PTZ_RIGHT = 1;
//    public static final int PTZ_UP = 8;
//    public static final int PTZ_DOWN = 4;
//    public static final int PTZ_UPLEFT = 10;
//    public static final int PTZ_UPRIGHT = 9;
//    public static final int PTZ_DOWNLEFT = 6;
//    public static final int PTZ_DOWNRIGHT = 5;
//    public static final int PTZ_STOP = 0;
//
//
//    //倍率放大缩小
//    public static final int ZOOM_IN = 20;
//    public static final int ZOOM_OUT = 10;
//
//    //F1指令----start
//    public static final int IRIS_REDUCE = 48;
//    public static final int IRIS_GROW = 44;
//    public static final int FOCUS_NEAR = 42;
//    public static final int FOCUS_FAR = 41;
//    public static final int IRISE_AND_FOCUS_STOP = 40;
    //F1指令----stop


    //云镜控制
    PTZ_LEFT(2,"PTZ_LEFT"),
    PTZ_RIGHT(1,"PTZ_RIGHT"),
    PTZ_UP(8,"PTZ_UP"),
    PTZ_DOWN(4,"PTZ_DOWN"),
    PTZ_UPLEFT(10,"PTZ_UPLEFT"),
    PTZ_UPRIGHT(9,"PTZ_UPRIGHT"),
    PTZ_DOWNLEFT(6,"PTZ_DOWNLEFT"),
    PTZ_DOWNRIGHT(5,"PTZ_DOWNRIGHT"),
    //倍率缩小
    ZOOM_IN(20,"ZOOM_IN"),
    //倍率放大
    ZOOM_OUT(10,"ZOOM_OUT"),

    PTZ_STOP(0,"PTZ_STOP"),
    //预置位
    PRESET_SET(129,"PRESET_SET"),
    PRESET_INVOKE(130,"PRESET_INVOKE"),
    PRESET_DEL(131,"PRESET_DEL"),

    //F1 指令
    //光圈缩小放大
    IRIS_REDUCE(48,"IRIS_REDUCE"),
    IRIS_GROW(44,"IRIS_GROW"),
    //聚焦近远
    FOCUS_FAR(41,"FOCUS_FAR"),
    FOCUS_NEAR(42,"FOCUS_NEAR"),
    //F1停止stop
    IRISE_AND_FOCUS_STOP(40,"IRISE_AND_FOCUS_STOP"),

    ;
    private final Integer code;
    private final String typeName;

    public static PtzOperationTypeEnum getTypeByTypeId(Integer id){
        for (PtzOperationTypeEnum videoType : values()){
            if (videoType.code.equals(id)){
                return videoType;
            }
        }
        return null;
    }
}